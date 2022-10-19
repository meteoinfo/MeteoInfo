import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geo.mapdata.MapDataManage;
import org.meteoinfo.geo.meteodata.DrawMeteoData;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.ShapeTypes;

import java.awt.*;

public class ClipTest {

    public static void main(String[] args) {
        try {
            new ClipTest().run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() throws Exception {
        //绘制色斑图
        String fileFullName = "D:\\Temp\\test\\2022042720.120";
        MeteoDataInfo meteoDataInfo = new MeteoDataInfo();
        meteoDataInfo.openMICAPSData(fileFullName);
        GridData grid = meteoDataInfo.getGridData();

        //创建图例 如把 5 设置成6及以上则不会报错，GridData最小与最大分别为 7.6 21.3
        double[] values = new double[]{
                3,
                5,
                7,
                9,
                11,
                13,
                15,
                17};
        Color[] colors = new Color[]{
                Color.pink,
                Color.cyan,
                Color.white,
                Color.blue,
                Color.green,
                Color.yellow,
                Color.red,
                Color.orange,
                Color.gray};

        double minData = 0;
        double maxData = 0;
        double[] maxMin = new double[2];
        if (!grid.getMaxMinValue(maxMin)) {
            maxData = maxMin[0];
            minData = maxMin[1];
        }
        if (values[0] < minData) {
            minData = values[0];
        }
        if (values[values.length - 1] > maxData) {
            maxData = values[values.length - 1];
        }

        //渐变值
        LegendScheme aLS = LegendManage.createGraduatedLegendScheme(values, colors,
                ShapeTypes.POLYGON, minData, maxData, false, grid.getDoubleMissingValue());
        //绘制图层
        VectorLayer layer = DrawMeteoData.createShadedLayer(grid, aLS, "Shaded_var", "var", false);

        //对绘制的图层进行裁剪
        VectorLayer clipShp = MapDataManage.readMapFile_ShapeFile("D:\\Temp\\test\\taizhou.shp");

        PolygonBreak pb = (PolygonBreak) clipShp.getLegendScheme().getLegendBreak(0);
        pb.setDrawFill(false);
        pb.setDrawOutline(true);
        pb.setOutlineSize(1);
        pb.setOutlineColor(Color.gray);

        VectorLayer newlayer = layer.clip(clipShp);
        //如果 vlaues设置的范围不一样会报异常，如果使用系统自生成的则正常。最小值小于等于5异常。
    }
}
