# capacitor-sip-linphone

Capacitor Plugin for SIP intergration using linphone SDK and CallKit

## Install

```bash
npm install capacitor-sip-linphone
npx cap sync
```

## API

<docgen-index>

* [`initialize()`](#initialize)
* [`login(...)`](#login)
* [`logout()`](#logout)
* [`call(...)`](#call)
* [`acceptCall()`](#acceptcall)
* [`hangUp()`](#hangup)
* [`addListener(string, ...)`](#addlistenerstring)
* [`addListener(SipEvent.AccountStateChanged, ...)`](#addlistenersipeventaccountstatechanged)
* [`addListener(SipEvent.CallStateChanged, ...)`](#addlistenersipeventcallstatechanged)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize()

```typescript
initialize() => Promise<void>
```

Initialize plugin state

--------------------


### login(...)

```typescript
login(options: SipLoginOptions) => Promise<void>
```

Make login to the SIP

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#siploginoptions">SipLoginOptions</a></code> |

--------------------


### logout()

```typescript
logout() => Promise<void>
```

Logout & terminate account

--------------------


### call(...)

```typescript
call(options: SipOutgoingCallOptions) => Promise<void>
```

Make outgoing call

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#sipoutgoingcalloptions">SipOutgoingCallOptions</a></code> |

--------------------


### acceptCall()

```typescript
acceptCall() => Promise<void>
```

Accept incoming call

--------------------


### hangUp()

```typescript
hangUp() => Promise<void>
```

Terminate current call

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (data: any) => void) => Promise<PluginListenerHandle>
```

addListener

| Param              | Type                                |
| ------------------ | ----------------------------------- |
| **`eventName`**    | <code>string</code>                 |
| **`listenerFunc`** | <code>(data: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener(SipEvent.AccountStateChanged, ...)

```typescript
addListener(eventName: SipEvent.AccountStateChanged, listenerFunc: (data: AccountStateChangedData) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                           |
| ------------------ | ---------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#sipevent">SipEvent.AccountStateChanged</a></code>                              |
| **`listenerFunc`** | <code>(data: <a href="#accountstatechangeddata">AccountStateChangedData</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener(SipEvent.CallStateChanged, ...)

```typescript
addListener(eventName: SipEvent.CallStateChanged, listenerFunc: (data: CallStateChangedData) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                     |
| ------------------ | ---------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#sipevent">SipEvent.CallStateChanged</a></code>                           |
| **`listenerFunc`** | <code>(data: <a href="#callstatechangeddata">CallStateChangedData</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

removeAllListeners

--------------------


### Interfaces


#### SipLoginOptions

| Prop            | Type                                 | Description                      |
| --------------- | ------------------------------------ | -------------------------------- |
| **`transport`** | <code>'TLS' \| 'TCP' \| 'UDP'</code> | By default "UDP"                 |
| **`domain`**    | <code>string</code>                  | SIP domain address               |
| **`username`**  | <code>string</code>                  | User login for authentication    |
| **`password`**  | <code>string</code>                  | User password for authentication |


#### SipOutgoingCallOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`address`** | <code>string</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### AccountStateChangedData

<code>{ isLoggedIn: boolean; "username": string; "voipToken": string; "remoteToken": string; }</code>


#### CallStateChangedData

<code>{ isCallRunning: boolean; isCallIncoming: boolean; isCallOutgoing: boolean; remoteAddress: string; incomingCallName: string; }</code>


### Enums


#### SipEvent

| Members                   | Value                                 |
| ------------------------- | ------------------------------------- |
| **`AccountStateChanged`** | <code>'SIPAccountStateChanged'</code> |
| **`CallStateChanged`**    | <code>'SIPCallStateChanged'</code>    |

</docgen-api>
