#include <string.h>
#include <jni.h>
#include <getopt.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/socket.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdatomic.h>
#include <time.h>

#include "byedpi/error.h"
#include "main.h"

extern int server_fd;

static atomic_bool g_is_running = false;
static atomic_bool g_stop_requested = false;

static pthread_mutex_t transition_mutex = PTHREAD_MUTEX_INITIALIZER;

struct params default_params = {
        .await_int = 10,
        .ipv6 = 1,
        .resolve = 1,
        .udp = 1,
        .max_open = 512,
        .bfsize = 16384,
        .baddr = { .in6 = { .sin6_family = AF_INET6 } },
        .laddr = { .in = { .sin_family = AF_INET } },
        .debug = 0
};

void reset_internal_state(void) {
    clear_params();
    params = default_params;

    optind = 1;
    opterr = 0;

#ifdef __APPLE__
    optreset = 1;
#endif
}

void free_args(int argc, char **argv) {
    if (!argv) return;
    for (int i = 0; i < argc; i++) {
        if (argv[i]) free(argv[i]);
    }
    free(argv);
}

long long current_time_sec() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(JNIEnv *env, __attribute__((unused)) jobject thiz, jobjectArray args) {

    pthread_mutex_lock(&transition_mutex);
    if (atomic_load(&g_is_running)) {
        LOG(LOG_S, "Proxy already running.");
        pthread_mutex_unlock(&transition_mutex);
        return -1;
    }

    if (!args) {
        pthread_mutex_unlock(&transition_mutex);
        return -1;
    }

    int argc = (*env)->GetArrayLength(env, args);
    char **argv = calloc(argc + 1, sizeof(char *));

    if (!argv) {
        pthread_mutex_unlock(&transition_mutex);
        return -1;
    }

    for (int i = 0; i < argc; i++) {
        jstring arg = (jstring) (*env)->GetObjectArrayElement(env, args, i);
        if (!arg) {
            free_args(i, argv);
            pthread_mutex_unlock(&transition_mutex);
            return -1;
        }

        const char *arg_str = (*env)->GetStringUTFChars(env, arg, 0);
        if (!arg_str) {
            free_args(i, argv);
            pthread_mutex_unlock(&transition_mutex);
            return -1;
        }

        argv[i] = strdup(arg_str);
        (*env)->ReleaseStringUTFChars(env, arg, arg_str);
        (*env)->DeleteLocalRef(env, arg);
    }

    atomic_store(&g_is_running, true);
    atomic_store(&g_stop_requested, false);

    signal(SIGPIPE, SIG_IGN);

    pthread_mutex_unlock(&transition_mutex);

    int result = 0;
    int crash_count = 0;
    long long start_time;
    long long run_duration;

    while (!atomic_load(&g_stop_requested)) {
        reset_internal_state();

        start_time = current_time_sec();

        result = main(argc, argv);

        run_duration = current_time_sec() - start_time;

        if (atomic_load(&g_stop_requested)) {
            LOG(LOG_S, "Proxy stopped by user.");
            break;
        }

        if (run_duration > 5) {
            LOG(LOG_S, "Proxy interrupted after %llds. Fast restart triggered.", run_duration);
            crash_count = 0;
            continue;
        }

        crash_count++;
        LOG(LOG_E, "Proxy crashed immediately (Code %d). Backoff active.", result);

        if (crash_count > 10) {
            LOG(LOG_E, "Persistent startup failure. Aborting.");
            result = -2;
            break;
        }

        sleep(1);
    }

    pthread_mutex_lock(&transition_mutex);
    atomic_store(&g_is_running, false);
    atomic_store(&g_stop_requested, false);
    free_args(argc, argv);
    pthread_mutex_unlock(&transition_mutex);

    return result;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(__attribute__((unused)) JNIEnv *env, __attribute__((unused)) jobject thiz) {
    if (!atomic_load(&g_is_running)) return 0;

    pthread_mutex_lock(&transition_mutex);
    LOG(LOG_S, "Stop requested.");

    atomic_store(&g_stop_requested, true);

    if (server_fd > 0) {
        shutdown(server_fd, SHUT_RDWR);
    }

    pthread_mutex_unlock(&transition_mutex);
    return 0;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniForceClose(__attribute__((unused)) JNIEnv *env, __attribute__((unused)) jobject thiz) {
    pthread_mutex_lock(&transition_mutex);

    atomic_store(&g_stop_requested, true);

    if (server_fd > 0) {
        close(server_fd);
        server_fd = -1;
    }

    pthread_mutex_unlock(&transition_mutex);
    return 0;
}