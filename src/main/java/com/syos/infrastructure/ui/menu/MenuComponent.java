package com.syos.infrastructure.ui.menu;

public interface MenuComponent {
    void display();
    void execute();
    String getName();
    boolean isComposite();
}