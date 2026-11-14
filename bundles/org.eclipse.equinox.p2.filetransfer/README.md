# org.eclipse.equinox.p2.filetransfer

p2's HTTP file transfer. It replaces the dependency on the
[Eclipse Communication Framework (ECF)](https://github.com/eclipse-ecf/ecf):
the minimal set of ECF file transfer code needed by p2 and
[Oomph](https://github.com/eclipse-oomph/oomph) was copied here and trimmed.

| Bundle | Content | Copied from |
|---|---|---|
| `org.eclipse.equinox.p2.filetransfer` | file transfer API, URLConnection fallback, `java.net.http` provider (default) | `org.eclipse.ecf`, `org.eclipse.ecf.identity`, `org.eclipse.ecf.filetransfer`, `org.eclipse.ecf.provider.filetransfer`, `org.eclipse.ecf.provider.filetransfer.httpclientjava` |
| `org.eclipse.equinox.p2.filetransfer.httpclient5` | Apache HttpClient 5 provider | `org.eclipse.ecf.provider.filetransfer.httpclient5` |
| `org.eclipse.equinox.p2.filetransfer.httpclient5.win32` | Windows NTLM/SPNEGO for the HttpClient 5 provider | `org.eclipse.ecf.provider.filetransfer.httpclient5.win32` |

All packages are internal. The ones other bundles need are exported with
`x-friends`, including `org.eclipse.oomph.p2.core` and
`org.eclipse.oomph.setup.core`.

What was left out of the copy:

* containers (`ContainerFactory`, `IContainer`, `BaseContainer`) and the
  adapter factories registered with the platform adapter manager,
* the ECF extension points (`retrieveFileTransferProtocolFactory`,
  `browseFileTransferProtocolFactory`, `sendFileTransferProtocolFactory`,
  `org.eclipse.ecf.identity.namespace`, `urlStreamHandlerService`),
* sending files (outgoing transfers), socket events, the `org.eclipse.ecf.ssl`
  and `org.eclipse.ecf.provider.filetransfer.ssl` fragments, and all ID types
  except the file transfer ones.

## Selecting the HTTP client

Providers register an
`org.eclipse.equinox.internal.p2.filetransfer.service.IFileTransferProvider`
OSGi service (declarative services). For each protocol the provider with the
smallest priority number wins:

| Provider id | Bundle | Priority |
|---|---|---|
| `httpclientjava` | `org.eclipse.equinox.p2.filetransfer` | 42 (default) |
| `httpclient5` | `org.eclipse.equinox.p2.filetransfer.httpclient5` | 43 |

To use another provider, name it in the system property

    -Dorg.eclipse.equinox.p2.filetransfer.httpProvider=httpclient5

This replaces
`-Dorg.eclipse.ecf.provider.filetransfer.excludeContributors=org.eclipse.ecf.provider.filetransfer.httpclientjava`,
which is no longer read.

The other system properties keep their ECF names, e.g.
`org.eclipse.ecf.provider.filetransfer.retrieve.readTimeout`,
`org.eclipse.ecf.provider.filetransfer.httpclient4.retrieve.connectTimeout`,
`org.eclipse.ecf.provider.filetransfer.httpclient5.retrieve.connectionTTL`,
`org.eclipse.ecf.provider.filetransfer.httpclientjava.sharedClient` and
`org.eclipse.ecf.http.cache.max-age`.

## Migrating Oomph

### Manifests

`org.eclipse.oomph.p2.core` and `org.eclipse.oomph.setup.core`: replace

    org.eclipse.ecf;bundle-version="[3.1.0,4.0.0)",
    org.eclipse.ecf.identity;bundle-version="[3.1.0,4.0.0)",
    org.eclipse.ecf.filetransfer;bundle-version="[5.0.0,6.0.0)",
    org.eclipse.ecf.provider.filetransfer;bundle-version="[3.1.0,4.0.0)"

with

    org.eclipse.equinox.p2.filetransfer;bundle-version="[1.0.0,2.0.0)"

`org.eclipse.oomph.setup.installer` and `org.eclipse.oomph.p2.tests` can keep
requiring `org.eclipse.equinox.p2.transport.ecf`; the bundle kept its name.

### Packages

| ECF package | New package |
|---|---|
| `org.eclipse.ecf.core.security` | `org.eclipse.equinox.internal.p2.filetransfer.core.security` |
| `org.eclipse.ecf.core.util` | `org.eclipse.equinox.internal.p2.filetransfer.core.util` |
| `org.eclipse.ecf.core.identity` | `org.eclipse.equinox.internal.p2.filetransfer.core.identity` |
| `org.eclipse.ecf.filetransfer` | `org.eclipse.equinox.internal.p2.filetransfer` |
| `org.eclipse.ecf.filetransfer.events` | `org.eclipse.equinox.internal.p2.filetransfer.events` |
| `org.eclipse.ecf.filetransfer.identity` | `org.eclipse.equinox.internal.p2.filetransfer.identity` |
| `org.eclipse.ecf.filetransfer.service` | `org.eclipse.equinox.internal.p2.filetransfer.service` |
| `org.eclipse.ecf.provider.filetransfer` | `org.eclipse.equinox.internal.p2.filetransfer.provider` |
| `org.eclipse.ecf.provider.filetransfer.browse` | `org.eclipse.equinox.internal.p2.filetransfer.provider.browse` |
| `org.eclipse.ecf.provider.filetransfer.identity` | `org.eclipse.equinox.internal.p2.filetransfer.provider.identity` |
| `org.eclipse.ecf.provider.filetransfer.retrieve` | `org.eclipse.equinox.internal.p2.filetransfer.provider.retrieve` |
| `org.eclipse.ecf.provider.filetransfer.util` | `org.eclipse.equinox.internal.p2.filetransfer.provider.util` |

The class names did not change. Everything Oomph uses is still there:
`ConnectContextFactory`, `IConnectContext`, `ECFException`, `Proxy`,
`ProxyAddress`, `IRetrieveFileTransferContainerAdapter`,
`IRemoteFileSystemBrowserContainerAdapter`, `IRetrieveFileTransferOptions`,
`IFileTransferListener`, `IRemoteFileSystemListener`, `IIncomingFileTransfer`,
`IRemoteFile`, `IRemoteFileInfo`, `IncomingFileTransferException`,
`BrowseFileTransferException`, `UserCancelledException`, the events,
`IFileID`, `FileTransferID`, `FileTransferNamespace` and `ProxySetupHelper`.

### Getting an adapter (`ECFURIHandlerImpl`)

There are no containers any more. Replace

```java
IContainer container = ContainerFactory.getDefault().createContainer();
IRetrieveFileTransferContainerAdapter fileTransfer = container.getAdapter(IRetrieveFileTransferContainerAdapter.class);
IRemoteFileSystemBrowserContainerAdapter browser = container.getAdapter(IRemoteFileSystemBrowserContainerAdapter.class);
```

with

```java
IRetrieveFileTransferContainerAdapter fileTransfer = FileTransfers.newRetrieveAdapter();
IRemoteFileSystemBrowserContainerAdapter browser = FileTransfers.newBrowseAdapter();
```

(`org.eclipse.equinox.internal.p2.filetransfer.FileTransfers`).
`ContainerCreateException` no longer exists and does not need to be caught.

The file IDs are created as before:

```java
new FileTransferID(new FileTransferNamespace(), uri)
```

### Thread-safe adapters (`P2CorePlugin`)

`FileTransfers.newRetrieveAdapter()` and `FileTransfers.newBrowseAdapter()`
return a new adapter on every call, also the ones used by p2's `FileReader` and
`FileInfoReader`. The workaround in `P2CorePlugin.Implementation.start()` that
registers its own `MultiProtocolRetrieveAdapterFactory` and
`MultiProtocolFileSystemBrowserAdapterFactory` subclasses with
`ECFPlugin.getDefault().getAdapterManager()` is not needed any more and has to
be removed: these factory classes and `ECFPlugin` no longer exist.

### Installer (`Installer.p2.inf`)

Replace

    setProgramProperty(propName:org.eclipse.ecf.provider.filetransfer.excludeContributors,propValue:org.eclipse.ecf.provider.filetransfer.httpclientjava)

with

    setProgramProperty(propName:org.eclipse.equinox.p2.filetransfer.httpProvider,propValue:httpclient5)

and the same for the `unconfigure` instruction. Do the same for
`-Dorg.eclipse.ecf.provider.filetransfer.excludeContributors=` in
`MarketplaceAnalyzer.jenkinsfile` (an empty value there means the default
provider, so the property can simply be dropped).

### Cookies

`ECFURIHandlerImpl` reads the cookie store of the HttpClient 5 provider by
reflection: the `httpClient` field of the incoming transfer and the
`cookieStore` field of the Apache client. Both field names are unchanged in
`org.eclipse.equinox.internal.p2.filetransfer.httpclient5.HttpClientRetrieveFileTransfer`.
The cast to `org.apache.hc.client5.http.cookie.CookieStore` and the optional
import of `org.apache.hc.client5.http.cookie` stay as they are.

### Launch configurations and setups

Replace the ECF bundles (`org.eclipse.ecf`, `org.eclipse.ecf.identity`,
`org.eclipse.ecf.filetransfer`, `org.eclipse.ecf.provider.filetransfer*`,
`org.eclipse.ecf.ssl`) in the `.launch` files with
`org.eclipse.equinox.p2.filetransfer` and, if the HttpClient 5 provider is
wanted, `org.eclipse.equinox.p2.filetransfer.httpclient5`. The ECF features
listed in `setups/interim/PlatformConfiguration.setup` are not needed for p2
any more.
