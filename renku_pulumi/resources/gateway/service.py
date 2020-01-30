import pulumi
from pulumi_kubernetes.core.v1 import Service

def gateway_service(global_config, gateway_secret, gateway_configmap, gateway_deployment):
    config = pulumi.Config('gateway')
    gateway_values = config.require_object('values')

    global_values = pulumi.Config('global')
    global_values = global_values.require_object('values')

    k8s_config = pulumi.Config('kubernetes')

    stack = pulumi.get_stack()

    gateway_name = "{}-{}-gateway".format(stack, pulumi.get_project())

    gateway_metadata = {
        'labels':
            {
                'app': gateway_name,
                'release': stack
            },
        'name': gateway_name
    }

    servicePort = config.get_int('port') or 80

    global_config['gateway'] = gateway_values
    global_config['gateway']['service']['port'] = servicePort
    global_config['gateway']['name'] = gateway_name

    return Service(
        gateway_name,
        metadata=gateway_metadata,
        spec={
            'type': gateway_values['service']['type'],
            'ports':[
                {
                    'port': servicePort,
                    'targetPort': 'http',
                    'protocol': 'TCP',
                    'name': 'http'
                },
                {
                    'port': 8000,
                    'targetPort': 'http',
                    'protocol': 'TCP',
                    'name': 'admin'
                }
            ],
            'selector': gateway_metadata['labels']
        },
        opts=pulumi.ResourceOptions(depends_on=[gateway_secret, gateway_configmap, gateway_deployment])
    )


def gateway_auth_service(global_config, gateway_secret, gateway_configmap, gateway_auth_deployment, dependencies=[]):
    config = pulumi.Config('gateway')
    gateway_values = config.require_object('values')

    global_values = pulumi.Config('global')
    global_values = global_values.require_object('values')

    k8s_config = pulumi.Config('kubernetes')

    stack = pulumi.get_stack()

    gateway_name = "{}-{}-gateway".format(stack, pulumi.get_project())

    auth_name = '{}-auth'.format(gateway_name)
    auth_metadata = {
        'name': auth_name,
        'labels':
            {
                'app': auth_name,
                'release': stack
            }
    }

    return Service(
        auth_name,
        metadata=auth_metadata,
        spec={
            'type': gateway_values['service']['type'],
            'ports':[
                {
                    'port': 80,
                    'targetPort': 'http',
                    'protocol': 'TCP',
                    'name': 'http'
                }
            ],
            'selector': auth_metadata['labels']
        },
        opts=pulumi.ResourceOptions(depends_on=[gateway_secret, gateway_configmap, gateway_auth_deployment] + dependencies)
    )
