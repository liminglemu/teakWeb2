package com.teak.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据生成工具类
 */
@Component
@Slf4j
public class DataGeneratorUtil {
    
    private static final String[] FIRST_NAMES = {
        "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
        "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧"
    };
    
    private static final String[] LAST_NAMES = {
        "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
        "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "霞", "平",
        "刚", "桂英", "鹏", "华", "红", "丽梅", "龙", "飞", "丹", "玲"
    };
    
    private static final String[] CITIES = {
        "北京市", "上海市", "广州市", "深圳市", "杭州市", "南京市", "成都市", "武汉市",
        "西安市", "重庆市", "天津市", "苏州市", "郑州市", "青岛市", "长沙市", "沈阳市",
        "哈尔滨市", "西安市", "昆明市", "厦门市", "无锡市", "东莞市", "济南市", "合肥市",
        "南宁市", "石家庄市", "长春市", "南昌市", "福州市", "太原市"
    };
    
    private static final String[] DISTRICTS = {
        "朝阳区", "海淀区", "西城区", "东城区", "丰台区", "石景山区",
        "浦东新区", "黄浦区", "徐汇区", "长宁区", "静安区", "普陀区",
        "天河区", "越秀区", "荔湾区", "海珠区", "白云区", "黄埔区",
        "南山区", "福田区", "罗湖区", "宝安区", "龙岗区", "盐田区"
    };
    
    private static final String[] STREETS = {
        "中山路", "解放路", "人民路", "建设路", "胜利路", "光明路",
        "和平路", "文化路", "青年路", "幸福路", "繁荣路", "发展路",
        "创新路", "科技路", "创业路", "兴业路", "富强路", "和谐路"
    };
    
    private static final String[] PHONE_PREFIXES = {
        "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
        "150", "151", "152", "153", "155", "156", "157", "158", "159",
        "180", "181", "182", "183", "185", "186", "187", "188", "189"
    };
    
    private final Random random = new Random();
    
    /**
     * 生成随机姓名
     * @return 随机姓名
     */
    public String generateRandomName() {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return firstName + lastName;
    }
    
    /**
     * 生成随机地址
     * @return 随机地址
     */
    public String generateRandomAddress() {
        String city = CITIES[random.nextInt(CITIES.length)];
        String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
        String street = STREETS[random.nextInt(STREETS.length)];
        int houseNumber = random.nextInt(1000) + 1;
        return city + district + street + houseNumber + "号";
    }
    
    /**
     * 生成随机手机号
     * @return 随机手机号
     */
    public String generateRandomPhone() {
        String prefix = PHONE_PREFIXES[random.nextInt(PHONE_PREFIXES.length)];
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            suffix.append(random.nextInt(10));
        }
        return prefix + suffix.toString();
    }
    
    /**
     * 生成随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成指定数量的随机数据
     * @param count 数据数量
     * @return 随机数据列表
     */
    public List<String> generateRandomData(int count) {
        List<String> dataList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int type = random.nextInt(5);
            switch (type) {
                case 0:
                    dataList.add(generateRandomName());
                    break;
                case 1:
                    dataList.add(generateRandomAddress());
                    break;
                case 2:
                    dataList.add(generateRandomPhone());
                    break;
                case 3:
                    dataList.add(generateRandomString(random.nextInt(20) + 5));
                    break;
                default:
                    dataList.add("数据项_" + i);
                    break;
            }
        }
        return dataList;
    }
}