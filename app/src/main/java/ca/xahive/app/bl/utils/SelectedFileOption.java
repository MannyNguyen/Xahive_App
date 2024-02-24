package ca.xahive.app.bl.utils;


public class SelectedFileOption implements Comparable<SelectedFileOption>{
    private String name;
    private String data;
    private String path;

    public SelectedFileOption(String name, String data, String path)
    {
        this.name = name;
        this.data = data;
        this.path = path;
    }
    public String getName()
    {
        return name;
    }
    public String getData()
    {
        return data;
    }
    public String getPath()
    {
        return path;
    }
    @Override
    public int compareTo(SelectedFileOption o) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}

