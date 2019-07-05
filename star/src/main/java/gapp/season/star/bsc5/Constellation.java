package gapp.season.star.bsc5;

public enum Constellation {
    c1("室女座", "Vir"),
    c2("大熊座", "UMa"),
    c3("鲸鱼座", "Cet"),
    c4("武仙座", "Her"),
    c5("波江座", "Eri"),
    c6("飞马座", "Peg"),
    c7("天龙座", "Dra"),
    c8("半人马座", "Cen"),
    c9("宝瓶座", "Aqr"),
    c10("蛇夫座", "Oph"),
    c11("狮子座", "Leo"),
    c12("牧夫座", "Boo"),
    c13("双鱼座", "Psc"),
    c14("人马座", "Sgr"),
    c15("天鹅座", "Cyg"),
    c16("金牛座", "Tau"),
    c17("鹿豹座", "Cam"),
    c18("仙女座", "And"),
    c19("船尾座", "Pup"),
    c20("御夫座", "Aur"),
    c21("天鹰座", "Aql"),
    c22("巨蛇座", "Ser"),
    c23("英仙座", "Per"),
    c24("仙后座", "Cas"),
    c25("猎户座", "Ori"),
    c26("仙王座", "Cep"),
    c27("天猫座", "Lyn"),
    c28("天秤座", "Lib"),
    c29("双子座", "Gem"),
    c30("巨蟹座", "Cnc"),
    c31("船帆座", "Vel"),
    c32("天蝎座", "Sco"),
    c33("船底座", "Car"),
    c34("麒麟座", "Mon"),
    c35("玉夫座", "Scl"),
    c36("凤凰座", "Phe"),
    c37("猎犬座", "CVn"),
    c38("白羊座", "Ari"),
    c39("摩羯座", "Cap"),
    c40("天炉座", "For"),
    c41("后发座", "Com"),
    c42("大犬座", "CMa"),
    c43("孔雀座", "PaV"),
    c44("天鹤座", "Gru"),
    c45("豺狼座", "Lup"),
    c46("六分仪座", "Sex"),
    c47("杜鹃座", "Tuc"),
    c48("印第安座", "Ind"),
    c49("南极座", "Oct"),
    c50("天兔座", "Lep"),
    c51("天琴座", "Lyr"),
    c52("巨爵座", "Crt"),
    c53("天鸽座", "Col"),
    c54("狐狸座", "Vul"),
    c55("小熊座", "UMi"),
    c56("望远镜座", "Tel"),
    c57("时钟座", "Hor"),
    c58("绘架座", "Pic"),
    c59("南鱼座", "PsA"),
    c60("水蛇座", "Hyi"),
    c61("唧筒座", "Ant"),
    c62("天坛座", "Ara"),
    c63("小狮座", "LMi"),
    c64("罗盘座", "Pyx"),
    c65("显微镜座", "Mic"),
    c66("天燕座", "Aps"),
    c67("蝎虎座", "Lac"),
    c68("海豚座", "Del"),
    c69("乌鸦座", "Crv"),
    c70("小犬座", "CMi"),
    c71("北冕座", "CrB"),
    c72("剑鱼座", "Dor"),
    c73("矩尺座", "Nor"),
    c74("山案座", "Men"),
    c75("长蛇座", "Hya"),
    c76("飞鱼座", "Vol"),
    c77("苍蝇座", "Mus"),
    c78("三角座", "Tri"),
    c79("蜒蜓座", "Cha"),
    c80("南冕座", "CrA"),
    c81("雕具座", "Cae"),
    c82("网罟座", "Ret"),
    c83("南三角座", "TrA"),
    c84("盾牌座", "Sct"),
    c85("圆规座", "Cir"),
    c86("天箭座", "Sge"),
    c87("小马座", "Equ"),
    c88("南十字座", "Cru");
    private String nameCn;
    private String nameEn;

    Constellation(String nameCn, String nameEn) {
        this.nameCn = nameCn;
        this.nameEn = nameEn;
    }

    public static String getConstellationName(String starName) {
        if (starName != null) {
            starName = ((starName.length() == 10) ? starName.substring(7) : starName).toLowerCase(); //后3位为星座名
            for (Constellation constellation : values()) {
                if (starName.contains(constellation.nameEn.toLowerCase())) {
                    return constellation.nameCn;
                }
            }
        }
        return "";
    }
}
