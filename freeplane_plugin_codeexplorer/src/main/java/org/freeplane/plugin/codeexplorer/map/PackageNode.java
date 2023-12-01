package org.freeplane.plugin.codeexplorer.map;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;


class PackageNode extends CodeNode {
    static final String UI_ICON_NAME = "code_package";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(PackageNode.UI_ICON_NAME, "code/folder.svg");
    }
    private static boolean containsAnalyzedClassesInPackageTree(JavaPackage javaPackage) {
        return javaPackage.getClassesInPackageTree().stream().anyMatch(CodeNode::isClassSourceKnown);
    }

    private final JavaPackage javaPackage;
    private final long classCount;

	public PackageNode(final JavaPackage javaPackage, final MapModel map, String text) {
		super(map);
		this.javaPackage = javaPackage;
		this.classCount = javaPackage.getClassesInPackageTree().stream()
		        .filter(CodeNode::isClassSourceKnown)
		        .filter(CodeNode::isNamed)
		        .count();
		setID(javaPackage.getName());
		setText(text + formatClassCount(classCount));
        setFolded(classCount > 0);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

    @Override
    HasName getCodeElement() {
        return javaPackage;
    }

	@Override
    protected boolean initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (!children.isEmpty()|| classCount == 0)
	        return false;
	    final List<JavaPackage> packages = relevantSubpackages(javaPackage);
	    boolean hasSubpackages = ! packages.isEmpty();
	    boolean hasClasses = javaPackage.getClasses().stream().anyMatch(CodeNode::isClassSourceKnown);
	    if(! hasSubpackages)
	        return false;
	    GraphNodeSort<JavaPackage> childNodes = new GraphNodeSort<JavaPackage>();
	    for (JavaPackage childPackage : packages) {
	        childNodes.addNode(childPackage);
	        DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
	        Map<JavaPackage, Long> dependencies = childPackage.getClassDependenciesFromThisPackageTree().stream()
	                .filter(dep -> dep.getTargetClass().getSource().isPresent())
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(childPackage, e.getKey(), e.getValue()));
	    }
	    if(hasClasses) {
	        childNodes.addNode(javaPackage);
	        DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
	        Map<JavaPackage, Long> dependencies = javaPackage.getClassDependenciesFromThisPackage().stream()
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(javaPackage, e.getKey(), e.getValue()));
	    }

	    List<List<JavaPackage>> orderedPackages = childNodes.sortNodes();
	    for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
	        for (JavaPackage childPackage : orderedPackages.get(subgroupIndex)) {
	            final CodeNode node = createChildPackageNode(childPackage, "");
	            children.add(node);
	            node.setParent(this);
	        }
	    }
	    return true;
	}

    private static List<JavaPackage> relevantSubpackages(JavaPackage javaPackage) {
        return javaPackage.getSubpackages()
	            .stream()
	            .filter(PackageNode::containsAnalyzedClassesInPackageTree)
	            .collect(Collectors.toList());
    }

    private CodeNode createChildPackageNode(JavaPackage childPackage, String parentName) {
        String childPackageName = childPackage.getRelativeName();
        List<JavaPackage> subpackages = relevantSubpackages(childPackage);
        boolean samePackage = childPackage == javaPackage;
        if(samePackage || subpackages.isEmpty() && ! childPackage.getClasses().isEmpty()) {
            String childName = samePackage ? childPackageName + " - package" : parentName + childPackageName;
            return new ClassesNode(childPackage, getMap(), childName, samePackage);
        }
        else if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new PackageNode(childPackage, getMap(), parentName + childPackageName);
    }

    private JavaPackage getTargetChildNodePackage(Dependency dep) {
        JavaClass targetClass = dep.getTargetClass();
        return getChildNodePackage(targetClass);
    }

    private JavaPackage getChildNodePackage(JavaClass javaClass) {
        JavaPackage childNodePackage = javaClass.getPackage();
        if(childNodePackage.equals(javaPackage))
            return childNodePackage;
        for(;;) {
            Optional<JavaPackage> parent = childNodePackage.getParent();
            if(! parent.isPresent() || parent.get().equals(javaPackage))
                return childNodePackage;
            childNodePackage = parent.get();
        }

    }

	@Override
	public int getChildCount(){
	    if(classCount == 0)
	        return 0;
	    int knownCount = super.getChildrenInternal().size();
	    if(knownCount > 0)
	        return knownCount;
	    else
	        return javaPackage.getSubpackages().size() + (javaPackage.getClasses().isEmpty() ? 0 : 1);
	}



    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
	}

	@Override
	public boolean hasChildren() {
    	return classCount > 0;
	}


    @Override
	public String toString() {
		return getText();
	}

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return javaPackage.getClassDependenciesFromThisPackageTree().stream();
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getClassDependenciesToThisPackageTree().stream();
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    @Override
    Set<CodeNode> findCyclicDependencies() {
        CodeNode classes = (CodeNode) getMap().getNodeForID(getID() + ".package");
        if(classes != null)
            return classes.findCyclicDependencies();
        else
            return super.findCyclicDependencies();
    }
}
