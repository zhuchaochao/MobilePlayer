# MobilePlayer
  ## 说明：
```
一、这是学习尚硅谷的学习视频，敲出来的练手作品，所以使用的后台数据信息也是尚硅谷提供的。
二、作品app中网络视频模块和百思不得姐模块的数据是通过软件抓包抓到的。
```
  ## 演示说明：
* 主体框架是一个FrameLayout，四个Fragment，通过点击RidioButton,使用自定义switchPager(Fragment fromPage, BasePage nextPage)方法切换不同Fragment。
*switchPager方法如下：
 private void switchPager(Fragment fromPage, BasePage nextPage) {
        if(tempPager != nextPage){
            tempPager = nextPage;
            if(nextPage != null){
                FragmentTransaction  transaction = getSupportFragmentManager().beginTransaction();
                if(!nextPage.isAdded()){
                    if(fromPage != null){
                        transaction.hide(fromPage);
                    }
                    transaction.add(R.id.fl_main,nextPage).commit();
                }else{
                    if(fromPage != null){
                        transaction.hide(fromPage);
                    }
                    transaction.show(nextPage).commit();
                }
            }
        }
    }
*视频播放器做了两个，一个是自己封装的系统播放器，另一个是使用Vitamio的。
* 本地视频和本地音乐是通过使用系统提供的contentResolver类，查找URI为MediaStore.Video.Media.EXTERNAL_CONTENT_URI的数据。主要代码如下：
  private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                super.run();

                mediaItems = new ArrayList();

                ContentResolver contentResolver = mcontext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                String[] projection = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频的文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者
                };

               Cursor cursor  = contentResolver.query(uri, projection, null, null, null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0);//视频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
                //Handler发消息
                handler.sendEmptyMessage(0);
            }
        }.start();
    }
* 网络视频和百思不得姐模块请求网络数据使用的框架是xutil。


```
```
  ## 演示 


![](https://github.com/zhuchaochao/Images/raw/master/MobilePlayer/localVideo.gif)
![](https://github.com/zhuchaochao/Images/raw/master/MobilePlayer/SystemVideoPlayer.gif)
![](https://github.com/zhuchaochao/Images/raw/master/MobilePlayer/netVideo.gif)
![](https://github.com/zhuchaochao/Images/raw/master/MobilePlayer/budejie.gif)
![](https://github.com/zhuchaochao/Images/raw/master/MobilePlayer/doubleClickBack.gif)