package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method('POST')
        url('/check')
        body([
                age: 25
        ])
        headers {
            header('Content-Type', 'application/json;charset=UTF-8')
        }
    }
    response {
        status 200
        body([
                eligible: true
        ])
        headers {
            header('Content-Type', 'application/json;charset=UTF-8')
        }
    }
}
