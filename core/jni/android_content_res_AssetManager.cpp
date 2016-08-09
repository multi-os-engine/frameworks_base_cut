
#undef LOG_TAG
#define LOG_TAG "AssetManager"

#include <inttypes.h>
#include <jni.h>
#include <JNIHelp.h>
#include "CoreFoundation/CFBundle.h"
#include "core_jni_helpers.h"

#include <fcntl.h>
#include <stdio.h>

namespace android {

static void init(JNIEnv* env, jobject clazz, jboolean isSystem) {
    jclass cls = env->GetObjectClass(clazz);
    jfieldID fid = env->GetFieldID(cls, "mPaths", "[Ljava/lang/String;");
    jclass stringClass = env->FindClass( "java/lang/String" );
    jobjectArray tmp = env->NewObjectArray( 6, stringClass, 0 );
    
    CFStringRef resPathStr = CFURLGetString(CFBundleCopyResourcesDirectoryURL(CFBundleGetMainBundle()));
    int len = CFStringGetLength(resPathStr)+1;
    char *resPath = new char[len];
    CFStringGetCString(resPathStr, resPath, len, kCFStringEncodingMacRoman);
    
    jstring resPathJStr;
    char *resPathCopy = new char[len+30];
    strcpy(resPathCopy, &resPath[7]);
    resPathJStr = env->NewStringUTF(&resPath[7]);
    env->SetObjectArrayElement(tmp, 0, resPathJStr);
    resPathJStr = env->NewStringUTF(strcat(resPathCopy,"/Frameworks/"));
    env->SetObjectArrayElement(tmp, 1, resPathJStr);

    delete resPathCopy;
    delete resPath;
    
    char * tmpdir = getenv("MOE_TMP_DIR");
    char * tmpdirCopy = new char[strlen(tmpdir)+30];
    strcpy(tmpdirCopy, tmpdir);
    tmpdirCopy[strlen(tmpdir)-5]='\0';
    resPathJStr = env->NewStringUTF(tmpdir);
    env->SetObjectArrayElement(tmp, 5, resPathJStr);
    strcpy(tmpdirCopy, tmpdir);
    tmpdirCopy[strlen(tmpdir)-5]='\0';
    resPathJStr = env->NewStringUTF(strcat(tmpdirCopy,"/Documents/Inbox/"));
    env->SetObjectArrayElement(tmp, 3, resPathJStr);
    strcpy(tmpdirCopy, tmpdir);
    tmpdirCopy[strlen(tmpdir)-5]='\0';
    resPathJStr = env->NewStringUTF(strcat(tmpdirCopy,"/Library/"));
    env->SetObjectArrayElement(tmp, 4, resPathJStr);
    strcpy(tmpdirCopy, tmpdir);
    tmpdirCopy[strlen(tmpdir)-5]='\0';
    resPathJStr = env->NewStringUTF(strcat(tmpdirCopy,"/Documents/"));
    env->SetObjectArrayElement(tmp, 2, resPathJStr);
    
    delete tmpdirCopy;
    
    env->SetObjectField(clazz, fid, tmp);
}
    
static jlong openAsset(JNIEnv* env, jobject clazz, jstring fileName, jint accessMode){
    const char *nativeFileName = env->GetStringUTFChars(fileName, 0);
    jclass cls = env->GetObjectClass(clazz);
    jfieldID fid = env->GetFieldID(cls, "mPaths", "[Ljava/lang/String;");
    jobjectArray tmp = (jobjectArray) env->GetObjectField(clazz, fid);
    int numPaths = env->GetArrayLength(tmp);
    jstring *paths = new jstring[numPaths];
    
    
    for(int i=0;i<numPaths;i++){
        paths[i] = (jstring)env->GetObjectArrayElement(tmp, i);
    }

    int f = 0;
    
    for(int i=0; (i<numPaths) && (f <= 0);i++){
        const char *path = env->GetStringUTFChars(paths[i], 0);
        char *asset = new char[strlen(path) + strlen(nativeFileName)];
        strcpy(asset, path);
        strcat(asset, nativeFileName);
        f = open(asset, O_RDONLY);
        env->ReleaseStringUTFChars(paths[i], path);
        delete asset;
    }
    
    env->ReleaseStringUTFChars(fileName, nativeFileName);
    delete paths;
    
    return (jlong)f;
}
    
    static jstring findAsset(JNIEnv* env, jobject clazz, jstring fileName){
        const char *nativeFileName = env->GetStringUTFChars(fileName, 0);
        jclass cls = env->GetObjectClass(clazz);
        jfieldID fid = env->GetFieldID(cls, "mPaths", "[Ljava/lang/String;");
        jobjectArray tmp = (jobjectArray) env->GetObjectField(clazz, fid);
        int numPaths = env->GetArrayLength(tmp);
        jstring *paths = new jstring[numPaths];
        
        
        for(int i=0;i<numPaths;i++){
            paths[i] = (jstring)env->GetObjectArrayElement(tmp, i);
        }
        
        int f = 0;
        int findInd = 0;
        
        for(int i=0; (i<numPaths) && (f <= 0);i++){
            const char *path = env->GetStringUTFChars(paths[i], 0);
            char *asset = new char[strlen(path) + strlen(nativeFileName)];
            strcpy(asset, path);
            strcat(asset, "/");
            strcat(asset, nativeFileName);
            f = open(asset, O_RDONLY);
            if(f > 0 )
                findInd = i;
            env->ReleaseStringUTFChars(paths[i], path);
            delete asset;
        }
        
        jstring findPath = NULL;
        if(f>0){
            close(f);
            findPath = paths[findInd];
        }
        
        env->ReleaseStringUTFChars(fileName, nativeFileName);
        delete paths;
        
        return findPath;
    }
    
static void destroyAsset(JNIEnv* env, jobject clazz, jlong asset){
    close((long)asset);
}

static jlong getAssetLength(JNIEnv* env, jobject clazz, jlong asset){
    int pos = lseek(asset, 0, 1);
    FILE *f=fdopen((int)asset, "rb");
    fseek(f, 0, SEEK_END);
    jlong len = (jlong)ftell(f);
    lseek(asset, pos, 0);
    return len;
}

    static void destroy(JNIEnv* env, jobject clazz){
    }
    
    static jint readAssetChar(JNIEnv* env, jobject clazz, jlong asset){
        int pos = lseek((int)asset, 0, SEEK_CUR);
        FILE *f=fdopen((int)asset, "r");
        fseek(f, pos, 0);
        int s = fgetc(f);
        return (jint)s;
    }
    
    static jint readAsset(JNIEnv* env, jobject clazz, jlong asset, jbyteArray buf, jint off, jint len){
        jbyte *b = env->GetByteArrayElements(buf, 0);
        int n = 0;
        if(off != 0){
            n=pread((int)asset, b, len, off);
        } else {
            n=read((int)asset, b, len);
        }
        env->ReleaseByteArrayElements(buf, b, 0);
        return n;
    }
    
    static jlong seekAsset(JNIEnv* env, jobject clazz, jlong asset, jlong offset, jint whence){
        return lseek(asset, offset, whence);
    }
    
    static jlong getAssetRemainingLength(JNIEnv* env, jobject clazz, jlong asset){
        long curPos = lseek((int)asset, 0, 1);
        jlong len = (lseek(asset, 0, 2)-curPos);
        lseek(asset, curPos, 0);
        return len;
    }
    
    static jint getResourceIdentifier(JNIEnv* env, jobject clazz, jstring type, jstring name, jstring defPackage){
        const char *nativeName = env->GetStringUTFChars(name, 0);
        const char *nativeType = env->GetStringUTFChars(type, 0);
        char* resourceName = new char[strlen(nativeName)+strlen(nativeType)];
        strcpy(resourceName, nativeName);
        strcat(resourceName, ".");
        strcat(resourceName, nativeType);

        jclass cls = env->GetObjectClass(clazz);
        jfieldID fid = env->GetFieldID(cls, "mNameIds", "Ljava/util/HashMap;");
        jobject hashMap =  env->GetObjectField(clazz, fid);
        
        jclass c_Map = env->FindClass("java/util/HashMap");
        
        // initialize the Get Size method of Map
        jmethodID mPut = env->GetMethodID(c_Map, "put",
                                          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        jmethodID mSize = env->GetMethodID(c_Map, "size",
                                           "()I");
        jint k = env->CallIntMethod(hashMap, mSize)+1;
        
        jclass cInteger = env->FindClass("java/lang/Integer");
        jmethodID mInteger = env->GetMethodID(cInteger, "<init>", "(I)V");
        jobject keyObj = env->NewObject(cInteger, mInteger, k);
        
        
        jstring n = env->NewStringUTF(resourceName);
        
        env->CallObjectMethod(hashMap, mPut, keyObj, n);
        
        delete resourceName;
        
        env->SetObjectField(clazz, fid, hashMap);
        
        return k;
    }
    
    static jstring getResourceEntryName(JNIEnv* env, jobject clazz, jint resid){
        jclass cls = env->GetObjectClass(clazz);
        jfieldID fid = env->GetFieldID(cls, "mNameIds", "Ljava/util/HashMap;");
        jobject hashMap =  env->GetObjectField(clazz, fid);
        
        jclass c_Map = env->FindClass("java/util/HashMap");
        
        // initialize the Get Size method of Map
        jmethodID mGet = env->GetMethodID(c_Map, "get",
                                          "(Ljava/lang/Object;)Ljava/lang/Object;");
        
        jclass cInteger = env->FindClass("java/lang/Integer");
        jmethodID mInteger = env->GetMethodID(cInteger, "<init>", "(I)V");
        jobject keyObj = env->NewObject(cInteger, mInteger, resid);
        
        jstring n = (jstring)env->CallObjectMethod(hashMap, mGet, keyObj);
        
        const char *nativeNameType = env->GetStringUTFChars(n, 0);
        char * name = new char[strlen(nativeNameType)];
        strcpy(name, nativeNameType);
        char * type = strrchr(name, '.');
        name[type - name]='\0';
        jstring jstrName = env->NewStringUTF(name);
        
        delete name;
        return jstrName;
    }
    
    static jstring getResourceEntry(JNIEnv* env, jobject clazz, jint resid){
        jclass cls = env->GetObjectClass(clazz);
        jfieldID fid = env->GetFieldID(cls, "mNameIds", "Ljava/util/HashMap;");
        jobject hashMap =  env->GetObjectField(clazz, fid);
        
        jclass c_Map = env->FindClass("java/util/HashMap");
        
        // initialize the Get Size method of Map
        jmethodID mGet = env->GetMethodID(c_Map, "get",
                                          "(Ljava/lang/Object;)Ljava/lang/Object;");
        
        jclass cInteger = env->FindClass("java/lang/Integer");
        jmethodID mInteger = env->GetMethodID(cInteger, "<init>", "(I)V");
        jobject keyObj = env->NewObject(cInteger, mInteger, resid);
        
        jstring n = (jstring)env->CallObjectMethod(hashMap, mGet, keyObj);
        
        //const char *nativeNameType = env->GetStringUTFChars(n, 0);
        
        return n;
    }
    
    static jint loadResourceValue(JNIEnv* env, jobject clazz, jint ident, jshort density, jobject tv,
                                  jboolean resolve){
        jstring name = getResourceEntry(env, clazz, ident);
        jclass cTypedValue = env->GetObjectClass(tv);
        jfieldID fid = env->GetFieldID(cTypedValue, "string", "Ljava/lang/CharSequence;");
        
        env->SetObjectField(tv, fid, name);
        
        return 0;
    }
    
    static jlong openNonAssetNative(JNIEnv* env, jobject clazz, jint cookie, jstring fileName, jint accessMode){
        return openAsset(env, clazz, fileName, accessMode);
    }
    
    static void setConfiguration(jint mcc, jint mnc, jstring locale, jint orient, jint ts, jint density,
                                 jint keyb, jint keybHid, jint navig, jint screenWidth, jint screenHeight,
                                 jint smallestScreenWidthDp, jint screenWidthDp, jint screenHeightDp,
                                 jint screenLayout, jint uiMode, jint majorVersion){
        
    }
    
    
} // namespace android

//[XRT]: don't include register functions in namespace android
using namespace android;

static const JNINativeMethod sMethods[] =
{
    /* name, signature, funcPtr */
    { "init", "(Z)V", (void*)init},
    {"openAsset", "(Ljava/lang/String;I)J", (void*)openAsset},
    {"findAssetPath", "(Ljava/lang/String;)Ljava/lang/String;", (void*)findAsset},
    {"destroyAsset", "(J)V", (void*)destroyAsset},
    {"getAssetLength", "(J)J", (void*)getAssetLength},
    {"destroy", "()V", (void*)destroy},
    {"readAssetChar", "(J)I", (void*)readAssetChar},
    {"readAsset", "(J[BII)I", (void*)readAsset},
    {"seekAsset", "(JJI)J", (void*)seekAsset},
    {"getAssetRemainingLength","(J)J", (void*)getAssetRemainingLength},
    {"getResourceIdentifier", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void*)getResourceIdentifier},
    {"getResourceEntryName", "(I)Ljava/lang/String;", (void*)getResourceEntryName},
    {"loadResourceValue", "(ISLandroid/util/TypedValue;Z)I", (void*)loadResourceValue},
    {"openNonAssetNative", "(ILjava/lang/String;I)J", (void*)openNonAssetNative},
    {"setConfiguration", "(IILjava/lang/String;IIIIIIIIIIIIII)V",(void*)setConfiguration},
};

int register_android_content_res_AssetManager(JNIEnv* env)
{
    return RegisterMethodsOrDie(env, "android/content/res/AssetManager", sMethods, NELEM(sMethods));
}


