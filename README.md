# Building the Update Site

1. Navigate to the `com.example.demo.update-site` project or folder.
2. Open the `site.xml` file.
3. Click **Build All** (usually in the Eclipse Update Site Editor) to generate an update site.  
   - This will produce the touchpoint example feature and its plugins.
4. (Optional) To create an installable archive, zip the following output together:
   - `/features`
   - `/plugins`
   - `artifacts.jar`
   - `content.jar`

You can then distribute or install from this ZIP as an offline update site.
