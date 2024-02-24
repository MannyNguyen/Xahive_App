package ca.xahive.app.bl.local;

public class XYDimension {
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public XYDimension(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        boolean classOk = o.getClass().equals(this.getClass());

        if (classOk) {
            XYDimension xyd = (XYDimension)o;

            return (getX() == xyd.getX() && getY() == xyd.getY());
        }

        return false;
    }
}
