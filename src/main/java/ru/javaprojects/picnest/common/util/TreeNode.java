package ru.javaprojects.picnest.common.util;

import ru.javaprojects.picnest.common.HasIdAndParentId;

import java.util.List;

public interface TreeNode<T extends HasIdAndParentId, R extends TreeNode<T, R>> {
    List<R> subNodes();
}
