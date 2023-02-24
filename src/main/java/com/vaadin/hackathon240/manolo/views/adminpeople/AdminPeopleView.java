package com.vaadin.hackathon240.manolo.views.adminpeople;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.hackathon240.manolo.data.entity.Person;
import com.vaadin.hackathon240.manolo.data.service.PersonService;
import java.util.Optional;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Admin People")
@Route(value = "admin-people/:personID?/:action?(edit)")
@PermitAll
@Uses(Icon.class)
public class AdminPeopleView extends Div implements BeforeEnterObserver {

    private final String PERSON_ID = "personID";
    private final String PERSON_EDIT_ROUTE_TEMPLATE = "admin-people/%s/edit";

    private final Grid<Person> grid = new Grid<>(Person.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private TextField role;
    private Checkbox important;
    private TextField password;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private Button plus = new Button("+");

    private final BeanValidationBinder<Person> binder;

    private Person person;

    private final PersonService personService;

    private Div editorLayoutDiv;
    private ConfirmDialog dialog;

    public AdminPeopleView(PersonService personService) {
        this.personService = personService;
        addClassNames("admin-people-view");

        // Create UI

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(false);
        horizontalLayout.setSizeFull();


        createGridLayout(horizontalLayout);
        createEditorLayout(horizontalLayout);

        showDetail(false);

        add(horizontalLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        LitRenderer<Person> importantRenderer = LitRenderer.<Person>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.isImportant() ? "check" : "minus").withProperty("color",
                        important -> important.isImportant()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);

        grid.addColumn("password").setAutoWidth(true);
        grid.setItems(query -> personService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AdminPeopleView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Person.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);
        // Add actions to the buttons
        plus.addClickListener(e -> populateForm(null));
        cancel.addClickListener(e -> cancel());
        save.addClickListener(e -> {
            try {
                if (this.person == null) {
                    this.person = new Person();
                }
                binder.writeBean(this.person);
                personService.update(this.person);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(AdminPeopleView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent event) {
        UI.getCurrent().addShortcutListener(cancel::click, Key.ESCAPE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> personId = event.getRouteParameters().get(PERSON_ID).map(Long::parseLong);
        if (personId.isPresent()) {
            Optional<Person> personFromBackend = personService.get(personId.get());
            if (personFromBackend.isPresent()) {
                populateForm(personFromBackend.get());
            } else {
                Notification.show(String.format("The requested person was not found, ID = %s", personId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AdminPeopleView.class);
            }
        }
    }

    private void createEditorLayout(HorizontalLayout splitLayout) {
        editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        editorLayoutDiv.setClassName("detail flex flex-col bg-contrast-5");
        editorLayoutDiv.setMaxWidth("350px");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date Of Birth");
        occupation = new TextField("Occupation");
        role = new TextField("Role");
        important = new Checkbox("Important");
        password = new TextField("Password");
        formLayout.add(firstName, lastName, email, phone, dateOfBirth, occupation, role, important, password);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.add(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN); //TODO: remove
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(HorizontalLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.add(wrapper);
        wrapper.add(grid);
        splitLayout.add(wrapper);
        plus.setClassName("fab-button");
        plus.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        wrapper.add(plus);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void cancel() {
        if (binder.hasChanges()) {
            dialog.open();
        } else {
            clearForm();
        }
    }

    private void clearForm() {
        populateForm(null);
        showDetail(false);
    }

    private void populateForm(Person value) {
        this.person = value;
        binder.readBean(this.person);
        showDetail(true);
    }

    private void showDetail(boolean show) {
        editorLayoutDiv.setVisible(show);
        plus.setVisible(!show);
    }
}
