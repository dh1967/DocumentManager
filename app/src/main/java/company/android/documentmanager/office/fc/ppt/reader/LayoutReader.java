/*
 * 文件名称:           LayoutReader.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:04:50
 */
package company.android.documentmanager.office.fc.ppt.reader;

import java.io.InputStream;
import java.util.Iterator;

import company.android.documentmanager.office.fc.dom4j.Document;
import company.android.documentmanager.office.fc.dom4j.Element;
import company.android.documentmanager.office.fc.dom4j.io.SAXReader;
import company.android.documentmanager.office.fc.openxml4j.opc.PackagePart;
import company.android.documentmanager.office.fc.openxml4j.opc.ZipPackage;
import company.android.documentmanager.office.fc.ppt.ShapeManage;
import company.android.documentmanager.office.pg.model.PGLayout;
import company.android.documentmanager.office.pg.model.PGMaster;
import company.android.documentmanager.office.pg.model.PGModel;
import company.android.documentmanager.office.pg.model.PGPlaceholderUtil;
import company.android.documentmanager.office.pg.model.PGSlide;
import company.android.documentmanager.office.pg.model.PGStyle;
import company.android.documentmanager.office.system.IControl;

/**
 * 解析 layout
 * <p>
 * <p>
 * Read版本:       Read V1.0
 * <p>
 * 作者:           jhy1790
 * <p>
 * 日期:           2012-3-2
 * <p>
 * 负责人:         jhy1790
 * <p>
 * 负责小组:         
 * <p>
 * <p>
 */
public class LayoutReader
{
    private static LayoutReader layoutReader = new LayoutReader();
    
    /**
     * 
     */
    public static LayoutReader instance()
    {
        return layoutReader;
    }
  
    /**
     * get PGLayout
     *  layoutPart
     * @return
     */
    public PGLayout getLayouts(IControl control, ZipPackage zipPackage, PackagePart layoutPart, PGModel pgModel, 
        PGMaster pgMaster, PGStyle defaultStyle) throws Exception
    {
        // layout xml
        SAXReader saxreader = new SAXReader();
        InputStream in = layoutPart.getInputStream();
        Document poiLayout = saxreader.read(in);
        Element layout = poiLayout.getRootElement();
        PGLayout pgLayout = null;
        if (layout != null)
        {
            pgLayout = new PGLayout();
            if (layout.attribute("showMasterSp") != null)
            {
                String val = layout.attributeValue("showMasterSp");
                if (val != null && val.length() > 0 && Integer.valueOf(val) == 0)
                {
                    pgLayout.setAddShapes(false);
                }    
            }
            Element cSld = layout.element("cSld");
            if (cSld != null)
            {   
                Element spTree = cSld.element("spTree");
                if (spTree != null)
                {
                    // background
                    processBackgroundAndFill(control, zipPackage, layoutPart, pgMaster, pgLayout, cSld);
                    // text style
                    processTextStyle(control, layoutPart, pgMaster, pgLayout, spTree);
                    
                    // slidemaster
                    PGSlide pgSlide = new PGSlide();
                    pgSlide.setSlideType(PGSlide.Slide_Layout);
                    for (Iterator< ? > it = spTree.elementIterator(); it.hasNext();)
                    {
                        ShapeManage.instance().processShape(control, zipPackage, layoutPart, null, 
                            pgMaster, pgLayout, defaultStyle,  pgSlide,PGSlide.Slide_Layout, (Element)it.next(), null, 1.0f, 1.0f);
                    }
                    if (pgSlide.getShapeCount() > 0)
                    {
                        pgLayout.setSlideMasterIndex(pgModel.appendSlideMaster(pgSlide));
                    }
                }
            }
        }
        in.close();
        return pgLayout;
    }
  
    /**
     * 获取 sp 位置
     *  layoutPart
     */
    private void processTextStyle(IControl control, PackagePart layoutPart, PGMaster pgMaster, PGLayout pgLayout, Element spTree)
    {
        for (Iterator< ? > it = spTree.elementIterator(); it.hasNext();)
        {
            Element sp = (Element)it.next();
            String type = ReaderKit.instance().getPlaceholderType(sp);
            int idx = ReaderKit.instance().getPlaceholderIdx(sp);
            Element txBody = sp.element("txBody");
            if (txBody != null)
            {
                Element lstStyle = txBody.element("lstStyle");
                StyleReader.instance().setStyleIndex(style);
                if (!PGPlaceholderUtil.instance().isBody(type))
                {
                    pgLayout.setStyleByType(type, StyleReader.instance().getStyles(control, pgMaster, sp, lstStyle));
                }
                else if (idx > 0)
                {
                    pgLayout.setStyleByIdx(idx, StyleReader.instance().getStyles(control, pgMaster, sp, lstStyle));  
                }
                
                style = StyleReader.instance().getStyleIndex();
            }
        }
    }
    
    /**
     * set background
     * @throws Exception 
     */
    private void processBackgroundAndFill(IControl control, ZipPackage zipPackage, PackagePart layoutPart, 
        PGMaster pgMaster, PGLayout pgLayout, Element cSld) throws Exception
    {
        Element bg = cSld.element("bg");
        if (bg != null)
        {
            pgLayout.setBackgroundAndFill(BackgroundReader.instance().getBackground(control,
                zipPackage, layoutPart, pgMaster, bg));
        }
    }
    
    /**
     * 
     */
    public void dispose()
    {
        style = 1001;
    }
    
    //
    private int style = 1001;
}
