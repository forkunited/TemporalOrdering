#include <jni.h>
#include <stdio.h>
#include "HelloJNI.h"
 
// Implementation of native method sayHello() of HelloJNI class
JNIEXPORT void JNICALL Java_HelloJNI_sayHello(JNIEnv *env, jobject thisObj, jobject arrayObj) {
	printf("Hello World!\n");
	jint *inCArray = (*env)->GetIntArrayElements(env, arrayObj, NULL);
	if (NULL == inCArray) return;
	jsize length = (*env)->GetArrayLength(env, arrayObj);

	printf("%d", length);

	return;
}
