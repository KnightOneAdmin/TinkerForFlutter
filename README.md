TinkerForFlutter

> Tinker针对Flutter热修复需要的插件

## 原理

在Android里面，Flutter打包之后的产物是一个.so文件（libapp.so）,Tinker热更新支持so文件更新，自然也就支持Flutter热更新。
理论上这样就可行了，不需要我们额外处理，但是Flutter初始化加载libapp.so时候，不去加载Tinker下载的补丁so,需要我们在初始化时候手动传入下。

正常在初始化时候，用FlutterShellArgs把so路径当参数传入即可，或者用反射传入也一样，都能实现热修复的效果。

可以这样处理：

初始化传参方式：

```
FlutterShellArgs flutterShellArgs = new FlutterShellArgs(new String[0]);
//libPath 就是libapp.so Tinker 下载补丁本地路径
if (!TextUtils.isEmpty(libPath)) {
    flutterShellArgs.add("--aot-shared-library-name=" + libPath);
    flutterShellArgs.add("--aot-shared-library-name="
            + getApplicationInfo(mPlatform.getApplication().getApplicationContext()).nativeLibraryDir
            + File.separator + libPath);
}

FlutterMain.ensureInitializationComplete(
        mPlatform.getApplication().getApplicationContext(), flutterShellArgs.toArray());
```

反射的方式：

```
try {
    Field field = FlutterMain.class.getDeclaredField("sAotSharedLibraryName");
    field.setAccessible(true);
    field.set(null, libPath);
} catch (Exception e) {
    e.printStackTrace();
}      
```

那么问题来了，既然上面手动实现就几行代码就能实现了，为啥还要用插件实现呢？

1.插件实现可以和Flutter解耦，不需要侵入项目。

2.由于FlutterBoost的存在，很多人都在使用它，并不能直接修改Flutter的初始化流程，需要修改源码。

插件就可以把两者统一了。

## 使用

根目录build.gradle引入插件
```
classpath 'me.chunsheng:tinker4flutter:0.0.2'
```

app目录build.gradle使用插件
```
apply plugin: 'tinker-for-flutter'


添加依赖库：
implementation 'me.chunsheng:hookflutter:0.0.2'
```

##插件效果

![Hook](hook.png?raw=true "Hook")

## 注意

本Demo只做测试用，生产环境使用，还需要根据源码按自己需求修改。

比如：hookflutter库里面根据自己需求加载不同abi下的libapp.so
     。
     Flutter初始化只Hook了一个startInitialization
     。
     等等


## 下发补丁

![Before](device-2020-08-17-113232.png?raw=true "Before")

![After](device-2020-08-17-113335.png?raw=true "After")

## 参考

https://cloud.tencent.com/developer/article/1531498

https://github.com/magicbaby810/HotfixFlutter

