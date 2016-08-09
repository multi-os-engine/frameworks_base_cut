package android.content.pm;

public class PackageManagerImpl extends PackageManager{
    private PackageInfo pi;

    public PackageManagerImpl(){
        pi = new PackageInfo();
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        pi.packageName = packageName;
        return pi;
    }
}
