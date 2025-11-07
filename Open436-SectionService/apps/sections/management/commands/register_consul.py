"""
Django management command to register service with Consul
"""
from django.core.management.base import BaseCommand
from apps.core.consul_client import consul_client


class Command(BaseCommand):
    help = '注册服务到 Consul'
    
    def handle(self, *args, **options):
        self.stdout.write('Registering service to Consul...')
        
        success = consul_client.register_service()
        
        if success:
            self.stdout.write(self.style.SUCCESS('✓ Service registered successfully'))
        else:
            self.stdout.write(self.style.ERROR('✗ Service registration failed'))

