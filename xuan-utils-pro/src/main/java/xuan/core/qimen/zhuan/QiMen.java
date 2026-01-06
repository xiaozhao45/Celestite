package xuan.core.qimen.zhuan;

import com.nlf.calendar.JieQi;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;

import java.util.Date;
import java.util.List;

import xuan.core.qimen.zhuan.maps.QiMenZhuanPanJiChuMap;
import xuan.core.qimen.zhuan.settings.QiMenZhuanPanJiChuSetting;
import xuan.core.qimen.zhuan.utils.QiMenZhuanPanJiChuUtil;

public class QiMen {

    public QiMenZhuanPanJiChuSetting qiMenZhuanPanJiChuSetting;

    /**
     * 公历日期（Solar型，如：2024-01-01）
     */
    public Solar solar;
    /**
     * 农历日期（Lunar型，如：二〇二三年冬月二十）
     */
    public Lunar lunar;
    /**
     * 公历日期（String型，如：2024-01-01 00:00:00）
     */
    public String solarStr;
    /**
     * 农历日期（String型，如：2023-11-20 00:00:00）
     */
    public String lunarStr;
    /**
     * 公历日期（String型，如：2024年01月01日00时00分00秒）
     */
    public String solarStr2;
    /**
     * 农历日期（String型，如：二〇二三年冬月二十(早)子时）
     */
    public String lunarStr2;
    /**
     * 公历日期（Date型，如：Mon Jan 01 00:00:00 CST 2024）
     */
    public Date solarDate;
    /**
     * 农历日期（Date型，如：Mon Nov 20 00:00:00 CST 2023）
     */
    public Date lunarDate;

    /**
     * 年干
     */
    public String yearGan;
    /**
     * 月干
     */
    public String monthGan;
    /**
     * 日干
     */
    public String dayGan;
    /**
     * 时干
     */
    public String hourGan;

    /**
     * 年支
     */
    public String yearZhi;
    /**
     * 月支
     */
    public String monthZhi;
    /**
     * 日支
     */
    public String dayZhi;
    /**
     * 时支
     */
    public String hourZhi;

    /**
     * 年干支
     */
    public String yearGanZhi;
    /**
     * 月干支
     */
    public String monthGanZhi;
    /**
     * 日干支
     */
    public String dayGanZhi;
    /**
     * 时干支
     */
    public String hourGanZhi;

    /**
     * 符头
     */
    public String fuTou;
    /**
     * 节气
     */
    public String jieQi;
    /**
     * 三元
     */
    public String sanYuan;
    /**
     * 阴阳遁
     */
    public String yinYangDun;
    /**
     * 局数
     */
    public int juShu;
    /**
     * 旬首
     */
    public String xunShou;
    /**
     * 旬首仪仗
     */
    public String xunShouYiZhang;
    /**
     * 旬首宫位
     */
    public int xunShouGongWei;
    /**
     * 地盘中的奇仪（1~9宫）
     */
    public List<String> diQiYi;
    /**
     * 地盘中的六甲（1~9宫）
     */
    public List<String> diLiuJia;
    /**
     * 值符
     */
    public String zhiFu;
    /**
     * 值使
     */
    public String zhiShi;

    /**
     * 值符旋转前宫位
     */
    public int oldZhiFuGongWei;
    /**
     * 值符旋转后宫位
     */
    public int newZhiFuGongWei;

    /**
     * 值使旋转前宫位
     */
    public int oldZhiShiGongWei;
    /**
     * 值使旋转后宫位
     */
    public int newZhiShiGongWei;

    /**
     * 地盘（1~9宫）
     */
    public List<String> diPan;
    /**
     * 天盘（1~9宫）
     */
    public List<String> tianPan;
    /**
     * 人盘（1~9宫）
     */
    public List<String> renPan;
    /**
     * 神盘（1~9宫）
     */
    public List<String> shenPan;

    /**
     * 天盘旋转后九星携带的奇仪，只包含[天禽星]携带的奇仪（1~9宫）
     */
    public List<String> tianPanQiYiTianQinYes;
    /**
     * 天盘旋转后九星携带的奇仪，不包含[天禽星]携带的奇仪（1~9宫）
     */
    public List<String> tianPanQiYiTianQinNo;

    /**
     * 上一节
     */
    public JieQi prevJie;
    /**
     * 下一节
     */
    public JieQi nextJie;
    /**
     * 上一气
     */
    public JieQi prevQi;
    /**
     * 下一气
     */
    public JieQi nextQi;
    /**
     * 获取六甲旬空（1~9宫）
     *
     * @return 六甲旬空（如：[戌, 亥]）
     */
    public List<String> getLiuJiaXunKong() {
        return QiMenZhuanPanJiChuMap.LIU_JIA_XUN_KONG.get(this.xunShou);
    }

    /**
     * 获取六甲旬空宫位（1~9宫）
     *
     * @return 六甲旬空宫位（如：[6]）
     */
    public List<Integer> getLiuJiaXunKongGongWei() {
        return QiMenZhuanPanJiChuMap.LIU_JIA_XUN_KONG_GONG.get(getLiuJiaXunKong());
    }

    /**
     * 获取驿马宫位
     *
     * @return 驿马宫位（如：8）
     */
    public Integer getYiMaGongWei() {
        return QiMenZhuanPanJiChuMap.YI_MA_GONG.get(getYiMa());
    }


    /**
     * 获取驿马
     *
     * @return 驿马（如：寅）
     */
    public String getYiMa() {
        return QiMenZhuanPanJiChuMap.YI_MA.get(this.hourZhi);
    }


    public String getYueJiangShen() {
        return null;
    }

    /**
     * 获取六仪击刑（1~9宫）
     *
     * @return 六仪击刑（如：[↗己击刑（坤二宫）]）
     */
    public List<String> getLiuYiJiXing() {
        return QiMenZhuanPanJiChuUtil.getLiuYiJiXing(this.tianPanQiYiTianQinYes, this.tianPanQiYiTianQinNo);
    }

    /**
     * 获取奇仪入墓（1~9宫）
     *
     * @return 奇仪入墓（如：[↖辛入墓（巽四宫）]）
     */
    public List<String> getQiYiRuMu() {
        return QiMenZhuanPanJiChuUtil.getQiYiRuMu(this.tianPanQiYiTianQinYes, this.tianPanQiYiTianQinNo);
    }


    /**
     * 获取六仪击邢入墓状态，不包含[天禽星]携带的奇仪（1~9宫）
     *
     * @return 六仪击邢入墓状态，不包含[天禽星]携带的奇仪（如：[, 击邢, , 入墓, , , , , ]）
     */
    public List<String> getLiuYiJiXingRuMuTianQinNoStatus() {
        return QiMenZhuanPanJiChuUtil.getJiXingRuMuStatus(null, this.tianPanQiYiTianQinNo).get("jiXingRuMuLinkTianPanQiYiTianQinNo");
    }

    /**
     * 获取六仪击邢入墓状态，只包含[天禽星]携带的奇仪（1~9宫）
     *
     * @return 六仪击邢入墓状态，只包含[天禽星]携带的奇仪（如：[, , , , , , , , ]）
     */
    public List<String> getLiuYiJiXingRuMuTianQinYesStatus() {
        return QiMenZhuanPanJiChuUtil.getJiXingRuMuStatus(this.tianPanQiYiTianQinYes, null).get("jiXingRuMuLinkTianPanQiYiTianQinYes");
    }

    /**
     * 获取门迫状态（1~9宫）
     *
     * @return 门迫状态（如：[, , , , , , , , ]）
     */
    public List<String> getMenPoStatus() {
        return QiMenZhuanPanJiChuUtil.getMenPoStatus(this.renPan);
    }


    /**
     * 获取十干克应（1~9宫）
     *
     * @return 十干克应（如：）
     */
    public List<List<String>> getShiGanKeYing() {
        return QiMenZhuanPanJiChuUtil.getShiGanKeYing(this.diPan, this.tianPanQiYiTianQinYes, this.tianPanQiYiTianQinNo);
    }

    /**
     * 获取八门克应（1~9宫）
     *
     * @return 八门克应（如：）
     */
    public List<List<String>> getBaMenKeYing() {
        return QiMenZhuanPanJiChuUtil.getBaMenKeYing(this.renPan, this.diPan, this.tianPanQiYiTianQinYes, this.tianPanQiYiTianQinNo);
    }

    /**
     * 获取八门静应（1~9宫）
     *
     * @return 八门静应（如：）
     */
    public List<List<String>> getBaMenJingYing() {
        return QiMenZhuanPanJiChuUtil.getMenDongJingYing(QiMenZhuanPanJiChuMap.BA_MEN_KE_YING, this.renPan);
    }

    /**
     * 获取八门动应（1~9宫）
     *
     * @return 八门动应（如：）
     */
    public List<List<String>> getBaMenDongYing() {
        return QiMenZhuanPanJiChuUtil.getMenDongJingYing(QiMenZhuanPanJiChuMap.BA_MEN_DONG_YING, this.renPan);
    }

    /**
     * 获取星门克应（1~9宫）
     *
     * @return 星门克应（如：）
     */
    public List<List<String>> getXingMenKeYing() {
        return QiMenZhuanPanJiChuUtil.getXingMenKeYing(this.tianPan, this.renPan);
    }

    /**
     * 获取九星时应（1~9宫）
     *
     * @return 九星时应（如：）
     */
    public List<List<String>> getJiuXingShiYing() {
        return QiMenZhuanPanJiChuUtil.getJiuXingShiYing(this.hourZhi, this.tianPan);
    }


    /**
     * 获取八卦旺衰（1~9宫）
     *
     * @return 八卦旺衰（如：[[坎宫, 旺], [坤宫, 囚], [震宫, 相], [巽宫, 相], [中宫, ], [乾宫, 休], [兑宫, 休], [艮宫, 囚], [离宫, 死]]）
     */
    public List<List<String>> getBaGuaWangShuai() {
        return QiMenZhuanPanJiChuUtil.getBaGuaWangShuai(QiMenZhuanPanJiChuMap.DI_ZHI_JI_JIE.get(this.monthZhi));
    }

    /**
     * 获取八门旺衰（1~9宫）
     *
     * @return 八门旺衰（如：[[休门, 旺], [死门, 囚], [伤门, 相], [杜门, 相], [中宫, ], [开门, 休], [惊门, 休], [生门, 囚], [景门, 死]]）
     */
    public List<List<String>> getBaMenWangShuai() {
        return QiMenZhuanPanJiChuUtil.getBaMenWangShuai(this.renPan, QiMenZhuanPanJiChuMap.DI_ZHI_JI_JIE.get(this.monthZhi));
    }

    /**
     * 获取九星旺衰（1~9宫）
     *
     * @return 九星旺衰（如：[[天蓬, 相], [芮禽, 休休], [天冲, 废], [天辅, 废], [中宫, ], [天心, 旺], [天柱, 旺], [天任, 休], [天英, 囚]]）
     */
    public List<List<String>> getJiuXingWangShuai() {
        return QiMenZhuanPanJiChuUtil.getJiuXingWangShuai(this.tianPan, this.monthZhi);
    }


    /**
     * 获取八神落宫状态（1~9宫）
     *
     * @return 八神落宫状态（如：[[值符, 吉], [玄武, 凶], [太阴, 吉], [六合, 吉], [中宫, ], [九天, 吉], [九地, 吉], [螣蛇, 凶], [白虎, 凶]]）
     */
    public List<List<String>> getBaShenLuoGongStatus() {
        return QiMenZhuanPanJiChuUtil.getBaShenLuoGongStatus(this.shenPan);
    }

    /**
     * 获取八门落宫状态（1~9宫）
     *
     * @return 八门落宫状态（如：[[休门, 伏吟], [死门, 伏吟], [伤门, 伏吟], [杜门, 伏吟], [中宫, ], [开门, 伏吟], [惊门, 伏吟], [生门, 伏吟], [景门, 伏吟]]）
     */
    public List<List<String>> getBaMenLuoGongStatus() {
        return QiMenZhuanPanJiChuUtil.getBaMenLuoGongStatus(this.renPan);
    }

    /**
     * 获取九星落宫状态（1~9宫）
     *
     * @return 九星落宫状态（如：[[天蓬, 相], [芮禽, 相相], [天冲, 相], [天辅, 相], [中宫, ], [天心, 相], [天柱, 相], [天任, 相], [天英, 相]]）
     */
    public List<List<String>> getJiuXingLuoGongStatus() {
        return QiMenZhuanPanJiChuUtil.getJiuXingLuoGongStatus(this.tianPan);
    }


    /**
     * 获取地盘奇仪与落宫地支的关系
     *
     * @return 地盘奇仪与落宫地支的关系（如：[[[子, 胎], [, ]], [[未, 冠带], [申, 沐浴]], [[卯, 胎], [, ]], [[辰, 墓], [巳, 死]], [[, ], [, ]], [[戌, 衰], [亥, 帝旺]], [[酉, 长生], [, ]], [[丑, 养], [寅, 长生]], [[午, 长生], [, ]]]）
     */
    public List<List<List<String>>> getDiPanQiYiLuoGongLink() {
        return QiMenZhuanPanJiChuUtil.getDiPanQiYiLuoGongLink(this.diPan);
    }

    /**
     * 获取天盘奇仪与落宫地支的关系，只包含[天禽星]携带的奇仪（1~9宫）
     *
     * @return 天盘奇仪与落宫地支的关系，只包含[天禽星]携带的奇仪（如：[[[子, 胎], [, ]], [[未, 冠带], [申, 沐浴]], [[卯, 胎], [, ]], [[辰, 墓], [巳, 死]], [[, ], [, ]], [[戌, 衰], [亥, 帝旺]], [[酉, 长生], [, ]], [[丑, 养], [寅, 长生]], [[午, 长生], [, ]]]）
     */
    public List<List<List<String>>> getTianPanQiYiLuoGongTianQinYesLink() {
        return QiMenZhuanPanJiChuUtil.getTianPanQiYiLuoGongTianQinYesLink(this.tianPanQiYiTianQinYes);
    }

    /**
     * 获取天盘奇仪与落宫地支的关系，不包含[天禽星]携带的奇仪（1~9宫）
     *
     * @return 天盘奇仪与落宫地支的关系，不包含[天禽星]携带的奇仪（如：[[[, ], [, ]], [[未, 养], [申, 长生]], [[, ], [, ]], [[, ], [, ]], [[, ], [, ]], [[, ], [, ]], [[, ], [, ]], [[, ], [, ]], [[, ], [, ]]]）
     */
    public List<List<List<String>>> getTianPanQiYiLuoGongTianQinNoLink() {
        return QiMenZhuanPanJiChuUtil.getTianPanQiYiLuoGongTianQinNoLink(this.tianPanQiYiTianQinNo);
    }
}
