package gapp.season.star.solar;

import gapp.season.star.util.StarUtil;

public enum Solar {
    Earth(1, "earth", "地球", 0, 365.2422, 1, 0, 0),
    Sun(3, "sun", "太阳", 4.92E+12, 0, 0, 365.2422, 0), //星等[-26.73]
    Moon(2, "moon", "月亮", 1.20E+07, 27.32, 0, 27.32, 180), //星等[-12.7]
    Mercury(1, "mercury", "水星", 575.44, 88, 0.387, 116, 28.3), //星等[-1.9]
    Venus(1, "venus", "金星", 6918.31, 224.7, 0.723, 584, 48.5), //星等[-4.6]
    Mars(1, "mars", "火星", 1445.44, 687, 1.524, 780, 180), //星等[-2.9]
    Jupiter(1, "jupiter", "木星", 1445.44, 4332, 5.203, 398.88, 180), //星等[-2.9]
    Saturn(1, "saturn", "土星", 120.23, 10760, 9.555, 378.09, 180), //星等[-0.2]
    Uranus(1, "uranus", "天王星", 0.48, 30798, 19.218, 369.4, 180), //星等[5.8]
    Neptune(1, "neptune", "海王星", 0.072, 60328, 30.0611, 367.4535, 180); //星等[7.85]


    private int type; //0恒星，1行星，2月球，3太阳
    private String id;
    private String name;
    private double luminance; //实际亮度
    private double revolutionPeriod; //公转周期·天(月亮为绕地周期，行星为绕日周期)
    private double orbitRadius; //轨道半径(和地球轨道的比值)
    private double surroundPeriod; //绕地周期·天
    private double maxAngle; //与太阳最大夹角

    Solar(int type, String id, String name, double luminance, double revolutionPeriod, double orbitRadius,
          double surroundPeriod, double maxAngle) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.luminance = luminance;
        this.revolutionPeriod = revolutionPeriod;
        this.orbitRadius = orbitRadius;
        this.surroundPeriod = surroundPeriod;
        this.maxAngle = maxAngle;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLuminance() {
        return luminance;
    }

    public double getRevolutionPeriod() {
        return revolutionPeriod;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public double getSurroundPeriod() {
        return surroundPeriod;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    //由于日月五行的亮度值比较悬殊，实际星图中用模拟值显示
    public double getShowLuminance() {
        if (type == 3) {
            return 2400;
        } else if (type == 2) {
            return 1200;
        } else if (type == 1) {
            if (luminance > 1) { //缩小亮行星的显示大小：土星(109.6)~金星(831.8)
                return Math.sqrt(luminance) * 10;
            } else { //增大暗行星的显示大小：天王星(13.9)、海王星(5.4)
                return Math.sqrt(luminance) * 20;
            }
        }
        return 0;
    }

    public String getStarData() {
        StringBuilder sb = new StringBuilder();
        sb.append("太阳系天体：").append(id).append("\n");
        sb.append("天体名称：").append(name).append("\n");
        if (revolutionPeriod > 0)
            sb.append("公转周期：").append(StarUtil.formatNum(revolutionPeriod, 1)).append("天\n");
        if (orbitRadius > 0)
            sb.append("轨道半径：").append(StarUtil.formatNum(orbitRadius, 2)).append("天文单位\n");
        if (type == 3) {
            sb.append("光谱类型：G2V\n"); //太阳光谱类型G2V
            sb.append("绝对星等：4.83\n"); //太阳绝对星等4.83
        }
        sb.append("可视星等：").append(StarUtil.formatNum(StarUtil.getMag(luminance), 2)).append("\n");
        sb.append("最大亮度：").append(StarUtil.formatNum(luminance, 0));
        return sb.toString();
    }
}
