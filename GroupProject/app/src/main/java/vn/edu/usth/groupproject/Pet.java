package vn.edu.usth.groupproject;
public class Pet {
    private String name;
    private int price;

    public Pet(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
