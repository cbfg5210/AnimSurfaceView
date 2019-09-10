# AnimSurfaceView

由于项目需要播放帧动画，并且图片数量比较多而且大，为了避免 OOM 问题，需要使用 SurfaceView 来绘制图片实现帧动画效果。本 Demo 中因为是使用 Glide 来加载图片，每张图片加载时间一般要100多毫秒，如果帧动画的时间间隔要求小于这个时间的话，还需要对图片的加载做一些优化处理，如果对这个间隔时间要求没有那么小，那么直接就可以使用了。
