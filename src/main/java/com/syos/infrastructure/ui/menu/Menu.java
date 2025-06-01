package com.syos.infrastructure.ui.menu;

import java.util.*;

public class Menu implements MenuComponent {
    private final String name;
    private final List<MenuComponent> components;
    private final Map<String, MenuComponent> componentMap;

    public Menu(String name) {
        this.name = name;
        this.components = new ArrayList<>();
        this.componentMap = new HashMap<>();
    }

    public void add(MenuComponent component) {
        components.add(component);
        if (component instanceof MenuItem) {
            componentMap.put(((MenuItem) component).getKey(), component);
        }
    }

    public void remove(MenuComponent component) {
        components.remove(component);
        if (component instanceof MenuItem) {
            componentMap.remove(((MenuItem) component).getKey());
        }
    }

    @Override
    public void display() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(name);
        System.out.println("=".repeat(50));

        for (MenuComponent component : components) {
            component.display();
        }

        System.out.println("=".repeat(50));
    }

    @Override
    public void execute() {
        // Composite menu doesn't execute, it displays
        display();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    public MenuComponent getComponent(String key) {
        return componentMap.get(key.toUpperCase());
    }
}
