package org.jeecg.modules.system.util;

import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * * 对应部门的表,处理并查找树级数据
 * 
 * @Author: Steve
 * @Date: 2019-01-22
 */
public class FindsDepartsChildrenUtil {

    /**
     * * List<SysDepart> 转化为 List<SysDepartTreeModel>
     */
    public static List<SysDepartTreeModel> wrapTreeDataToTreeList(List<SysDepart> recordList) {
        // * 转化 平铺SysDepart 到 平铺SysDepartTreeModel
        List<DepartIdModel> idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> records = new ArrayList<>();

        for (int i = 0; i < recordList.size(); i++) {
            SysDepart depart = recordList.get(i);
            records.add(new SysDepartTreeModel(depart));
        }
        // * 转化为树
        List<SysDepartTreeModel> tree = findChildren(records, idList);
        // * 情况children为空的情况
        setEmptyChildrenAsNull(tree);
        return tree;
    }

    /**
     * * 获取 DepartIdModel
     * 
     * @param recordList
     * @return
     */
    public static List<DepartIdModel> wrapTreeDataToDepartIdTreeList(List<SysDepart> recordList) {
        List<DepartIdModel> idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> records = new ArrayList<>();

        for (int i = 0; i < recordList.size(); i++) {
            SysDepart depart = recordList.get(i);
            records.add(new SysDepartTreeModel(depart));
        }
        findChildren(records, idList);
        return idList;
    }

    /**
     * * 该方法是找到并封装顶级父类的节点到TreeList集合
     */
    private static List<SysDepartTreeModel> findChildren(
            List<SysDepartTreeModel> recordList,
            List<DepartIdModel> departIdList) {

        // * 初始化树
        List<SysDepartTreeModel> treeList = new ArrayList<>();

        // * 遍历 平铺的SysDepartTreeModel
        for (int i = 0; i < recordList.size(); i++) {
            SysDepartTreeModel branch = recordList.get(i);
            // * 如果没有父节点
            if (oConvertUtils.isEmpty(branch.getParentId())) {
                // * 将 顶级节点 添加到TreeList
                treeList.add(branch);
                // * SysDepartTreeModel 转化为 DepartIdModel
                DepartIdModel departIdModel = new DepartIdModel().convert(branch);
                // * 构建顶级节点平铺list
                departIdList.add(departIdModel);
            }
        }
        getGrandChildren(treeList, recordList, departIdList);

        return treeList;
    }

    /**
     * * 该方法是找到顶级父类下的所有子节点集合并封装到TreeList集合
     * 
     * @param treeList   已经初始化的树
     * @param recordList 平铺的SysDepartTreeModel
     * @param idList     已经初始化的id树
     */
    private static void getGrandChildren(
            List<SysDepartTreeModel> treeList,
            List<SysDepartTreeModel> recordList,
            List<DepartIdModel> idList) {

        for (int i = 0; i < treeList.size(); i++) {
            // * 提取顶级节点
            SysDepartTreeModel model = treeList.get(i);
            DepartIdModel idModel = idList.get(i);

            for (int i1 = 0; i1 < recordList.size(); i1++) {
                SysDepartTreeModel m = recordList.get(i1);
                // * 将子节点加入到父节点children中
                if (m.getParentId() != null && m.getParentId().equals(model.getId())) {
                    model.getChildren().add(m);
                    DepartIdModel dim = new DepartIdModel().convert(m);
                    idModel.getChildren().add(dim);
                }
            }
            getGrandChildren(treeList.get(i).getChildren(), recordList, idList.get(i).getChildren());
        }

    }

    /**
     * * 该方法是将子节点为空的List集合设置为Null值
     */
    private static void setEmptyChildrenAsNull(List<SysDepartTreeModel> treeList) {
        for (int i = 0; i < treeList.size(); i++) {
            SysDepartTreeModel model = treeList.get(i);
            // * children 必定不为 null
            if (model.getChildren().size() == 0) {
                model.setChildren(null);
                model.setIsLeaf(true);
            } else {
                setEmptyChildrenAsNull(model.getChildren());
                model.setIsLeaf(false);
            }
        }
    }
}
