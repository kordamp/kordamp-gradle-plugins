application {
    title = 'sample'
    startupGroups = ['sample']
    autoShutdown = true
}
mvcGroups {
    // MVC Group for "sample"
    'sample' {
        model      = 'org.example.SampleModel'
        view       = 'org.example.SampleView'
        controller = 'org.example.SampleController'
    }
}