package com.example.application.views.helloworld;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.mpr.LegacyWrapper;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class HelloWorldView extends HorizontalLayout {

    private TextField name;
    private Button sayHello;

    public HelloWorldView() {
        name = new TextField("Your name");
        sayHello = new Button("Say hello");
        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
        });
        // sayHello.addClickShortcut(Key.ENTER);

        setMargin(true);
        LegacyWrapper wrappedName = new LegacyWrapper(name);
        LegacyWrapper wrappedSayHello = new LegacyWrapper(sayHello);
        setVerticalComponentAlignment(Alignment.END, wrappedName,
                wrappedSayHello);

        add(wrappedName, wrappedSayHello);
    }

}
