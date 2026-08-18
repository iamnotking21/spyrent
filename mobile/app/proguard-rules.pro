# The API client builds its payloads with org.json and reflection is not used,
# so the defaults are enough. Keep the entry points R8 cannot see from the
# manifest alone.
-keep class com.spyrent.child.BootReceiver { *; }
-keep class com.spyrent.child.SiteBlockerService { *; }
-keep class com.spyrent.child.BlockerService { *; }
