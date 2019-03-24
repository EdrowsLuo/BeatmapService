package com.edlplan.audiov.core.option;

public class OptionEntry<T> {
    private T data;

    private String name;

    private String description = null;

    private String hintForEdit = null;

    public OptionEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHintForEdit() {
        return hintForEdit;
    }

    public void setHintForEdit(String hintForEdit) {
        this.hintForEdit = hintForEdit;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String checkString(String res) {
        if (data.getClass() == Integer.class) {
            try {
                Integer.parseInt(res);
            } catch (NumberFormatException e) {
                return "@not a integer";
            }
        }
        return null;
    }
}
