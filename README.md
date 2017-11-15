# DynamicProgress

最近项目需要写的一个带动画的滚动条，感觉还挺实用的。效果如下：
![](/GIF.gif)

想想Android自带的ProgressBar用法真是反人类，也不知道能不能实现，干脆就写了个自定义View
话说我是分别画的每一个内部的小四边形，写完了才发现可以用DashPathEffect来画 ![](/01.png)

提供了以下几个可自定义的属性

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="DynamicProgress">
        <!-- 前景颜色1 -->
        <attr name="color1" format="color"/>
        <!-- 前景颜色2 -->
        <attr name="color2" format="color"/>
        <!-- 小菱形的宽度 -->
        <attr name="unit_width" format="dimension"/>
        <!-- 小菱形运动周期 -->
        <attr name="period" format="integer"/>
    </declare-styleable>
</resources>
```

color1和color2是小四边形的颜色
unit_width是小四边形的宽度
period是指第n个小四边形移动到第n+1个四边形的位置上所用的时间

```java
    DynamicProgress dynamicProgress = findViewById(R.id.dynamicProgress);
    dynamicProgress.setProgress(70);
    dynamicProgress.start();
```
像原生的一样使用#setProgress(int)来设置进度值
默认是不做动画的，需要调用#start()来使小四边形运动起来



