import { WebPlugin } from '@capacitor/core';

import type {SipLoginOptions, SipOutgoingCallOptions, SipPhoneControlPlugin} from './definitions';

export class SipPhoneControlWeb
  extends WebPlugin
  implements SipPhoneControlPlugin
{
  acceptCall(): Promise<void> {
    return Promise.resolve(undefined);
  }

  call(_options: SipOutgoingCallOptions): Promise<void> {
    return Promise.resolve(undefined);
  }

  hangUp(): Promise<void> {
    return Promise.resolve(undefined);
  }

  initialize(): Promise<void> {
    return Promise.resolve(undefined);
  }

  login(_options: SipLoginOptions): Promise<void> {
    return Promise.resolve(undefined);
  }

  logout(): Promise<void> {
    return Promise.resolve(undefined);
  }
}
