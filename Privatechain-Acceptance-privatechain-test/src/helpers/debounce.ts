// @ts-nocheck
//// @ts-ignore
export function debounce(func:any, timeout = 300){
    // @ts-ignore
    let timer;
    return (...args:any) => {
        // @ts-ignore
      clearTimeout(timer);
      // @ts-ignore
      timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
  }
  