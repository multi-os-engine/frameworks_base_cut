
#undef LOG_TAG
#define LOG_TAG "ContextImpl"

#include <inttypes.h>
#include <jni.h>
#include <JNIHelp.h>
#include "core_jni_helpers.h"
#include "sys/resource.h"

namespace android {

    static void setThreadPriority(jint priority){
        setpriority(PRIO_DARWIN_THREAD, 0, priority);
    }
    

} // namespace android

//[XRT]: don't include register functions in namespace android
using namespace android;

static const JNINativeMethod sMethods[] =
{
    /* name, signature, funcPtr */
    { "setThreadPriority", "(I)V",
            (void*)setThreadPriority },
};

int register_android_os_Process(JNIEnv* env)
{
    return RegisterMethodsOrDie(env, "android/os/Process", sMethods, NELEM(sMethods));
}


