package com.zcc.mobileplayer.utils;

import com.zcc.mobileplayer.bean.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 朱超超 on 2017-04-06.
 * 作用：
 */
public class LyricUtils {

    private ArrayList<Lyric> lyrics;
    /**
     * 是否存在歌词
     */
    private boolean isExistsLyric = false;

    public boolean getIsExistsLyric(){
        return isExistsLyric;
    }
    public  void  readLyricFile(File file){
        if(file == null || !file.exists()){
            //歌词文件不存在
            lyrics = null;
        }else{
            lyrics = new ArrayList<>();
            isExistsLyric = true;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader   = new BufferedReader(new InputStreamReader(new FileInputStream(file),"GBK"));
                String line = "";
                while ((line = bufferedReader.readLine()) != null){
                     parsedLyric(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //2.排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @Override
                public int compare(Lyric lhs, Lyric rhs) {
                    if (lhs.getTimePoint() < rhs.getTimePoint()) {
                        return -1;
                    } else if (lhs.getTimePoint() > rhs.getTimePoint()) {
                        return 1;
                    } else {
                        return 0;
                    }

                }
            });

            //3.计算每句高亮显示的时间
            for(int i=0;i<lyrics.size();i++){
                Lyric oneLyric = lyrics.get(i);
                if(i+1 < lyrics.size()){
                    Lyric twoLyric = lyrics.get(i+1);
                    oneLyric.setSleepTime(twoLyric.getTimePoint()-oneLyric.getTimePoint());
                }
            }
        }

    }

    private void parsedLyric(String line) {
        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        if (pos1 == 0 && pos2 != -1) {
            //第一个时间戳
            //装时间
            long[] times = new long[getCountTag(line)];
            String strTime = line.substring(pos1 + 1, pos2);
            times[0] = strTime2LongTime(strTime);
            String content = line;
            int i = 1;
            //解析多时间戳情况下的歌词
            while (pos1 == 0 && pos2 != -1) {
                content = content.substring(pos2 + 1);
                pos1 = content.indexOf("[");
                pos2 = content.indexOf("]");
                //如果还有时间戳
                if (pos2 != -1) {
                    strTime = line.substring(pos1 + 1, pos2);
                    times[i] = strTime2LongTime(strTime);
                    if (times[i] == -1) {
                        //歌词出错！可能多写了符号“]”；
                        break;
                    }
                    i++;
                }
            }

            Lyric lyric;
            //把时间数组和文本关联起来，并且加入到集合中
            for(int j = 0;j < times.length;j++){

                if(times[j] !=0){//有时间戳
                    lyric = new Lyric();
                    lyric.setContent(content);
                    lyric.setTimePoint(times[j]);
                    //添加到集合中
                    lyrics.add(lyric);
                }

            }
        }
    }
    /**
     * 把String类型是时间转换成long类型
     * @param strTime 02:04.12
     * @return
     */
    private long strTime2LongTime(String strTime) {
        long result = -1;
        try{

            //1.把02:04.12按照:切割成02和04.12
            String[] s1 = strTime.split(":");
            //2.把04.12按照.切割成04和12
            String[] s2 = s1[1].split("\\.");

            //1.分
            long min = Long.parseLong(s1[0]);

            //2.秒
            long second = Long.parseLong(s2[0]);

            //3.毫秒
            long mil = Long.parseLong(s2[1]);

            result =  min * 60 * 1000 + second * 1000 + mil*10;
        }catch (Exception e){
            e.printStackTrace();
            result = -1;
        }

        return result;
    }

    /**
     * 判断有多少句歌词
     * @param line [02:04.12][03:37.32][00:59.73]我在这里欢笑
     * @return
     */
    private int getCountTag(String line) {
        int result = -1;
        String [] left = line.split("\\[");
        String [] right = line.split("\\]");

        if(left.length==0 && right.length ==0){
            result = 1;
        }else if(left.length > right.length){
            result = left.length;
        }else{
            result = right.length;
        }
        return result;
    }

    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
//            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    public ArrayList<Lyric> getLyrics() {
        return this.lyrics;
    }
}
