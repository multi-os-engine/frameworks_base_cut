
#undef LOG_TAG
#define LOG_TAG "ContextImpl"

#include <inttypes.h>
#include <jni.h>
#include <JNIHelp.h>
#include "CoreFoundation/CFBundle.h"
#include "CoreFoundation/CFUtilities.h"
#include "core_jni_helpers.h"

namespace android {

static jstring nativeGetPackageName(JNIEnv* env, jobject clazz) {
    CFStringRef packName = CFBundleGetIdentifier(CFBundleGetMainBundle());
    int len = CFStringGetLength(packName)+1;
    char *cstrPackName = new char[len];
    CFStringGetCString(packName, cstrPackName, len, kCFStringEncodingMacRoman);
    
    jstring res = env->NewStringUTF(cstrPackName);

    delete []cstrPackName;
    
    return res;
}
    
    static jstring getTmpDir(JNIEnv* env, jobject clazz){
        char * tmpdir = getenv("MOE_TMP_DIR");
        return env->NewStringUTF(tmpdir);
    }

} // namespace android

//[XRT]: don't include register functions in namespace android
using namespace android;

static const JNINativeMethod sMethods[] =
{
    /* name, signature, funcPtr */
    { "nativeGetPackageName", "()Ljava/lang/String;",
            (void*)nativeGetPackageName },
    {"getTmpDir", "()Ljava/lang/String;", (void*)getTmpDir},
};

int register_android_content_ContextImpl(JNIEnv* env)
{
    return RegisterMethodsOrDie(env, "android/content/ContextImpl", sMethods, NELEM(sMethods));
}


