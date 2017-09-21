package tw.edu.nctu.pcslab.sectionlistview;


public class DeviceCell implements Comparable<DeviceCell>{

    private String name;
    private String category;
    private boolean isSectionHeader;

    public DeviceCell(String name, String category)
    {
        this.name = name;
        this.category = category;
        isSectionHeader = false;
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public void setToSectionHeader()
    {
        isSectionHeader = true;
    }

    public boolean isSectionHeader()
    {
        return isSectionHeader;
    }

    @Override
    public int compareTo(DeviceCell other) {
        return this.category.compareTo(other.category);
    }

}
