package cn.wildfire.imshat.net.model.pojoVO;

import java.io.Serializable;
import java.util.List;

import cn.wildfire.imshat.net.model.pojo.TypeChildrenBean;




public class TypeTagVO implements Serializable {

   
    private int id;
    private String name;
    private int courseId;
    private int parentChapterId;
    private int order;
    private int visible;
    private List<TypeChildrenBean> children;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getParentChapterId() {
        return parentChapterId;
    }

    public void setParentChapterId(int parentChapterId) {
        this.parentChapterId = parentChapterId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public List<TypeChildrenBean> getChildren() {
        return children;
    }

    public void setChildren(List<TypeChildrenBean> children) {
        this.children = children;
    }


}