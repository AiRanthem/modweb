package cn.airanthem.modweb.service;

import org.springframework.stereotype.Service;

@Service
public class RequestContext {
    ThreadLocal<String> addressTL = new ThreadLocal<>();

    public void setAddress(String address) {
        addressTL.set(address);
    }

    public String getAddress() {
        return addressTL.get();
    }
}
