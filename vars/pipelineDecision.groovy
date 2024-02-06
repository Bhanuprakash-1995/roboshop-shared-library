#!groovy

//we are controlling with below fuction about which language and which deployment platform needs to selected because we are keeping all language deployment platform in one folder(mean in one centralised repository)

def decidePipeline(Map configMap){
    application = configMap.get('application')
    switch(application) {
        case 'nodejsVM':
            nodejsVM(configMap)    
            break
        case 'javaVM':
            javaVM(configMap)
            break
        case 'nodejsEKS':
            nodejsEKS(configMap)
            break
        default:
            error "Application is not recognised"
            break
    }
}