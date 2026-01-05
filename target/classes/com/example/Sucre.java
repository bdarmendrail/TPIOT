public class Sucre {
    
    private String nom;
    private int age;
    private double sucre;

    public Sucre(String nom, int age, double sucre) {
        this.nom = nom;
        this.age = age;
        this.sucre = sucre;
    }

    public String getNom() {
        return nom;
    }

    public int getAge() {
        return age;
    }

    public double getSucre() {
        return sucre;
    }

    public void setSucre(double sucre) {
        this.sucre = sucre;
    }
}