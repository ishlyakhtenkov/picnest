package ru.javaprojects.picnest.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.javaprojects.picnest.common.HasIdAndParentId;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.to.FileTo;
import ru.javaprojects.picnest.common.util.AppUtil;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.common.util.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppUtilTest {
    private static final String FILES_PATH = "/content/inputted-files/";
    private static final String DIR = "files/";
    private static final String ORIGINAL_FILE_NAME = "New project logo.png";
    private static final MockMultipartFile INPUTTED_FILE = new MockMultipartFile("inputtedFile", ORIGINAL_FILE_NAME,
            MediaType.IMAGE_PNG_VALUE, "new logo file content bytes".getBytes());

    private static final List<Node> nodes = new ArrayList<>();
    private static final List<TestTreeNode> treeNodes = new ArrayList<>();

    static {
        prepareNodeCollections();
    }

    @Test
    void createFileWhenInputtedFileIsNotEmpty() {
        File expected = new File(FileUtil.normalizePath(ORIGINAL_FILE_NAME), FILES_PATH +
                FileUtil.normalizePath(DIR + ORIGINAL_FILE_NAME));
        Supplier<FileTo> fileToExtractor = () -> new FileTo(null, null, INPUTTED_FILE, null);
        File created = AppUtil.createFile(fileToExtractor, FILES_PATH, DIR);
        Assertions.assertThat(created).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void createFileWhenInputtedFileIsEmpty() {
        File expected = new File(FileUtil.normalizePath(ORIGINAL_FILE_NAME), FILES_PATH +
                FileUtil.normalizePath(DIR + ORIGINAL_FILE_NAME));
        Supplier<FileTo> fileToExtractor = () -> new FileTo(ORIGINAL_FILE_NAME, null, null, new byte[] {1, 2, 3, 4});
        File created = AppUtil.createFile(fileToExtractor, FILES_PATH, DIR);
        Assertions.assertThat(created).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void makeTree() {
        assertEquals(treeNodes, AppUtil.makeTree(nodes, TestTreeNode::new));
    }

    private record TestTreeNode(Node node, List<TestTreeNode> subNodes) implements TreeNode<Node, TestTreeNode> {
        public TestTreeNode(Node node) {
            this(node, new LinkedList<>());
        }
    }

    private record Node(long id, Long parentId) implements HasIdAndParentId {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Long getParentId() {
            return parentId;
        }

        @Override
        public void setId(Long id) {}
    }

    private static void prepareNodeCollections() {
        Node parent1 = new Node(1, null);
        Node parent2 = new Node(2, null);
        Node parent3 = new Node(3, parent1.id);
        Node parent4 = new Node(4, parent3.id);
        Node parent5 = new Node(5, parent1.id);
        Node child1 = new Node(6, parent2.id);
        Node child2 = new Node(7, parent2.id);
        Node child3 = new Node(8, parent3.id);
        Node child4 = new Node(9, parent3.id);
        Node child5 = new Node(10, parent4.id);
        Node child6 = new Node(11, parent4.id);
        Node child7 = new Node(12, parent5.id);
        Node child8 = new Node(13, parent5.id);
        Node child9 = new Node(14, parent5.id);
        nodes.add(parent1);
        nodes.add(parent2);
        nodes.add(parent3);
        nodes.add(parent4);
        nodes.add(parent5);
        nodes.add(child1);
        nodes.add(child2);
        nodes.add(child3);
        nodes.add(child4);
        nodes.add(child5);
        nodes.add(child6);
        nodes.add(child7);
        nodes.add(child8);
        nodes.add(child9);

        TestTreeNode parent1TreeNode = new TestTreeNode(parent1);
        TestTreeNode parent2TreeNode = new TestTreeNode(parent2);
        TestTreeNode parent3TreeNode = new TestTreeNode(parent3);
        TestTreeNode parent4TreeNode = new TestTreeNode(parent4);
        TestTreeNode parent5TreeNode = new TestTreeNode(parent5);
        TestTreeNode child1TreeNode = new TestTreeNode(child1);
        TestTreeNode child2TreeNode = new TestTreeNode(child2);
        TestTreeNode child3TreeNode = new TestTreeNode(child3);
        TestTreeNode child4TreeNode = new TestTreeNode(child4);
        TestTreeNode child5TreeNode = new TestTreeNode(child5);
        TestTreeNode child6TreeNode = new TestTreeNode(child6);
        TestTreeNode child7TreeNode = new TestTreeNode(child7);
        TestTreeNode child8TreeNode = new TestTreeNode(child8);
        TestTreeNode child9TreeNode = new TestTreeNode(child9);

        parent1TreeNode.subNodes.add(parent3TreeNode);
        parent1TreeNode.subNodes.add(parent5TreeNode);
        parent2TreeNode.subNodes.add(child1TreeNode);
        parent2TreeNode.subNodes.add(child2TreeNode);
        parent3TreeNode.subNodes.add(parent4TreeNode);
        parent3TreeNode.subNodes.add(child3TreeNode);
        parent3TreeNode.subNodes.add(child4TreeNode);
        parent4TreeNode.subNodes.add(child5TreeNode);
        parent4TreeNode.subNodes.add(child6TreeNode);
        parent5TreeNode.subNodes.add(child7TreeNode);
        parent5TreeNode.subNodes.add(child8TreeNode);
        parent5TreeNode.subNodes.add(child9TreeNode);

        treeNodes.add(parent1TreeNode);
        treeNodes.add(parent2TreeNode);
    }
}
