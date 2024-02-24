package ca.xahive.app.bl.local;

import android.util.Log;

import java.util.ArrayList;

public class PasswordMemoryCache {
    private ArrayList<PasswordMemoryCacheValue> memoryCache;

    private ArrayList<PasswordMemoryCacheValue> getMemoryCache() {
        if (memoryCache == null) {
            memoryCache = new ArrayList<PasswordMemoryCacheValue>();
        }
        return memoryCache;
    }

    private PasswordMemoryCacheValue getCacheValueForIdentifierAndContext(int identifier, PasswordCacheContext context) {
        PasswordMemoryCacheValue cv = new PasswordMemoryCacheValue(identifier, context, null);

        int foundIdx = -1;
        ArrayList<PasswordMemoryCacheValue> cache = getMemoryCache();

        for (int i = 0 ; i < cache.size() ; i++) {
            PasswordMemoryCacheValue pmcv = cache.get(i);
            if (pmcv.equals(cv)) {
                foundIdx = i;
                break;
            }
        }

        return (foundIdx >= 0) ? cache.get(foundIdx) : null;
    }

    public void setPasswordForIdentifierInContext(String password, int identifier, PasswordCacheContext context) {
        PasswordMemoryCacheValue pmcv = getCacheValueForIdentifierAndContext(identifier, context);
        if (pmcv != null) {
            pmcv.setPassword(password);
        }
        else {

            getMemoryCache().add(new PasswordMemoryCacheValue(identifier, context, password));
            getCacheValueForIdentifierAndContext(identifier, context);
        }

    }

    public String getPasswordForIdentifierInContext(int identifier, PasswordCacheContext context) {
        PasswordMemoryCacheValue pmcv = getCacheValueForIdentifierAndContext(identifier, context);

        return (pmcv != null) ? pmcv.getPassword() : null;
    }

    public void clear() {
        memoryCache = null;
    }

    private class PasswordMemoryCacheValue {
        private String password;
        private int identifier;
        private PasswordCacheContext context;

        public PasswordMemoryCacheValue(int identifier, PasswordCacheContext context, String password) {
            this.identifier = identifier;
            this.context = context;
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public PasswordCacheContext getContext() {
            return context;
        }

        public int getIdentifier() {
            return identifier;
        }

        public boolean equals(Object object) {
            if (object instanceof PasswordMemoryCacheValue) {
                PasswordMemoryCacheValue pmcv = (PasswordMemoryCacheValue)object;

                boolean ctxOk = (pmcv.getContext() != null && pmcv.getContext().equals(this.getContext()));
                boolean identOk = (pmcv.getIdentifier() == this.getIdentifier());

                return (ctxOk && identOk);
            }

            return false;
        }
    }
}
