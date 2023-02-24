package com.example.application;

import com.example.application.bean.Department;
import com.example.application.bean.DepartmentData;
import com.example.application.bean.DummyFile;
import com.example.application.service.DummyFileService;
import com.vaadin.componentfactory.explorer.ExplorerTreeGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;


/**
 * Example with a text and an icon
 */
@Route(value = "")
public class ExplorerView extends VerticalLayout {

    private DepartmentData departmentData = new DepartmentData();

    // Form
    private Binder<Department> binder = new Binder<>(Department.class);
    private IntegerField id = new IntegerField("id");
    private TextField name = new TextField("name");
    private TextField manager = new TextField("manager");
    private Checkbox archive = new Checkbox("archive");

    // Actions
    private Button removeButton = new Button("Remove selected Item");
    private Button addButton = new Button("Add child Item");

    private int nextid = 300;

    public ExplorerView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        TreeGrid<Department> grid = buildGrid();
        addAndExpand(grid);
        binder.bindInstanceFields(this);
        // every time the value is updated, update the node in the tree
        binder.addValueChangeListener(event -> {
            grid.getDataProvider().refreshItem(binder.getBean());
        });
        id.setReadOnly(true);

        HorizontalLayout formLayout = new HorizontalLayout(id, name, manager, archive, removeButton, addButton);
        formLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        formLayout.setPadding(true);
        add(formLayout);
        grid.addSelectionListener(selection ->
                selection.getFirstSelectedItem().ifPresent(selected ->
                        binder.setBean(selected)));

        configureActions(grid);
    }

    private void configureActions(TreeGrid<Department> grid) {
        // remove the item and deep refresh the parent
        removeButton.addClickListener(buttonClickEvent -> {
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(
                    selected -> {
                        departmentData.getDepartments().remove(selected);
                        if (binder.getBean().getParent() != null) {
                            grid.getDataProvider().refreshItem(binder.getBean().getParent(), true);
                        } else {
                            grid.getDataProvider().refreshAll();
                        }
                    }
            );
        });
        // add a child item to the selected item and deep refresh the selected item
        addButton.addClickListener(buttonClickEvent -> {
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(
                    selected -> {
                        Department department = new Department(nextid, "New child "+ nextid, selected,"");
                        nextid++;
                        departmentData.getDepartments().add(department);
                        grid.getDataProvider().refreshItem(selected,true);
                    }
            );
        });
    }

    private TreeGrid<Department> buildGrid() {
        ExplorerTreeGrid<Department> grid = new ExplorerTreeGrid<>();
        HierarchicalDataProvider<Department, Void> dataProvider =
                new AbstractBackEndHierarchicalDataProvider<Department, Void>() {

                    @Override
                    public int getChildCount(HierarchicalQuery<Department, Void> query) {
                        return departmentData.getChildDepartments(query.getParent()).size();
                    }

                    @Override
                    public boolean hasChildren(Department item) {
                        return departmentData.hasChildren(item);
                    }

                    @Override
                    protected Stream<Department> fetchChildrenFromBackEnd(
                            HierarchicalQuery<Department, Void> query) {
                        return departmentData.getChildDepartments(query.getParent(), query.getOffset()).stream();
                    }
                };

        grid.setDataProvider(dataProvider);
        grid.addComponentHierarchyColumn(value -> {
            Span span = new Span();
            if (value.isArchive()) {
                span.add(VaadinIcon.ARCHIVE.create());
            }
            span.add(new Span(value.getName()));
            return span;
        }).setHeader("Department Name");
        grid.setWidthFull();
        grid.expand(departmentData.getRootDepartments());
        return grid;
    }
}
