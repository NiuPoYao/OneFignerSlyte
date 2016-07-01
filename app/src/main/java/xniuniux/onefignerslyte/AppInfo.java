package xniuniux.onefignerslyte;

import android.support.annotation.Nullable;

public class AppInfo {

    private long id;
    private boolean isGeneral;
    private Integer positionGeneral = null;
    private String packageName;
    private String iconPath;
    private String highlightPath;

    public AppInfo(){
    }

    public AppInfo(long id, boolean isGeneral,
                   @Nullable Integer positionGeneral, String packageName,
                   String iconPath, String highlightPath){
        this.id = id;
        this.isGeneral = isGeneral;
        this.positionGeneral = positionGeneral;
        this.packageName = packageName;
        this.iconPath = iconPath;
        this.highlightPath = highlightPath;

    }

    public void setId(long id){
        this.id = id;
    }

    public void setIsGeneral(boolean b){
        this.isGeneral = b;
    }

    public void setPositionGeneral(Integer pos){
        this.positionGeneral = pos;
    }

    public void setPackageName(String name){
        this.packageName = name;
    }

    public void setIconPath(String path){
        this.iconPath = path;
    }

    public void setHighlightPath(String path){
        this.highlightPath = path;
    }


    public long getId(){
        return this.id;
    }

    public boolean isGeneral(){
        return this.isGeneral;
    }

    public Integer getPositionGeneral(){
        return this.positionGeneral;
    }

    public String getPackageName(){
        return this.packageName;
    }

    public String getIconPath(){
        return this.iconPath;
    }

    public String getHighlightPath(){
        return this.highlightPath;
    }

}
