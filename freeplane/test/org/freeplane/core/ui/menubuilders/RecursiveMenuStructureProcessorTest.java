package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveMenuStructureProcessorTest {

	private RecursiveMenuStructureProcessor recursiveMenuStructureBuilder;
	private EntryVisitor builder;
	private EntryVisitor defaultBuilder;

	@Before
	public void setup(){
		recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		defaultBuilder = Mockito.mock(EntryVisitor.class);
		builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		recursiveMenuStructureBuilder.addBuilder("emptyBuilder", EntryVisitor.EMTPY_VISITOR);
		recursiveMenuStructureBuilder.addBuilder("defaultBuilder", defaultBuilder);
	}


	@Test
	public void explicitBuilderIsCalled() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder", builder);
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		recursiveMenuStructureBuilder.process(entry);
		
		verify(builder).visit(entry);
	}

	@Test
	public void defaultBuilderIsCalled() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		EntryVisitor builder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.setDefaultBuilder(builder);
		final Entry entry = new Entry();
		recursiveMenuStructureBuilder.process(entry);
		verify(builder).visit(entry);
	}


	@Test
	public void defaultBuilderIsCalledForChild() {
		recursiveMenuStructureBuilder.addBuilder("builder", EntryVisitor.EMTPY_VISITOR);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder", "defaultBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		recursiveMenuStructureBuilder.process(entry);
		
		verify(defaultBuilder).visit(childEntry);
	}

	@Test
	public void defaultBuilderIsRestoredAfterChildCall() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		recursiveMenuStructureBuilder.addBuilder("builder1", EntryVisitor.EMTPY_VISITOR);
		recursiveMenuStructureBuilder.addBuilder("builder2", EntryVisitor.EMTPY_VISITOR);
		EntryVisitor defaultBuilder = Mockito.mock(EntryVisitor.class);
		recursiveMenuStructureBuilder.addBuilder("builder3", defaultBuilder);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder1", "builder3");
		recursiveMenuStructureBuilder.addBuilder("builder2", EntryVisitor.EMTPY_VISITOR);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("builder2", "builder2");

		final Entry entry = new Entry();
		entry.setBuilders(asList("builder1"));
		final Entry childEntry1 = new Entry();
		childEntry1.setBuilders(asList("builder2"));
		entry.addChild(childEntry1);
		
		final Entry childEntry2 = new Entry();
		entry.addChild(childEntry2);

		recursiveMenuStructureBuilder.process(entry);
		
		Mockito.verify(defaultBuilder).visit(childEntry2);
	}


	@Test
	public void defaultBuilderIsCalledForChildUsingDefaultBuilder() {
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("emptyBuilder", "defaultBuilder");
		recursiveMenuStructureBuilder.addBuilder("emptyBuilder2", EntryVisitor.EMTPY_VISITOR);
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("emptyBuilder2", "emptyBuilder");
		final Entry entry = new Entry();
		entry.setBuilders(asList("emptyBuilder2"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		final Entry subChildEntry = new Entry();
		childEntry.addChild(subChildEntry);
		
		recursiveMenuStructureBuilder.process(entry);
		
		Mockito.verify(defaultBuilder).visit(subChildEntry);
	}
	
	@Test(expected = IllegalStateException.class)
	public void defaultBuilderIsNotSetException() {
		final RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		final Entry childEntry = new Entry();
		recursiveMenuStructureBuilder.process(childEntry);
		
	}
	@Test
	public void defaultBuilderIsNotCalledForChildIfChildProcessingIsSkipped() {
		recursiveMenuStructureBuilder.setDefaultBuilder(defaultBuilder);
		final Entry entry = new Entry();
		when(builder.shouldSkipChildren(entry)).thenReturn(true);
		entry.setBuilders(asList("builder"));
		final Entry childEntry = new Entry();
		entry.addChild(childEntry);
		
		
		recursiveMenuStructureBuilder.process(entry);
		
		Mockito.verify(defaultBuilder, never()).visit(childEntry);
	}
}
