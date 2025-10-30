use serde::{Deserialize, Serialize};
use reqwest::Client;
use std::time::Duration;

#[derive(Serialize, Deserialize)]
pub struct ServiceRegistration {
    #[serde(rename = "ID")]
    pub id: String,
    #[serde(rename = "Name")]
    pub name: String,
    #[serde(rename = "Port")]
    pub port: u16,
    #[serde(rename = "Address")]
    pub address: String,
    #[serde(rename = "Check")]
    pub check: HealthCheck,
}

#[derive(Serialize, Deserialize)]
pub struct HealthCheck {
    #[serde(rename = "HTTP")]
    pub http: String,
    #[serde(rename = "Interval")]
    pub interval: String,
    #[serde(rename = "Timeout")]
    pub timeout: String,
}

#[derive(Clone)]
pub struct ConsulClient {
    client: Client,
    consul_url: String,
}

impl ConsulClient {
    pub fn new(consul_url: String) -> Self {
        Self {
            client: Client::builder()
                .timeout(Duration::from_secs(5))
                .build()
                .unwrap(),
            consul_url,
        }
    }

    pub async fn register_service(&self, registration: ServiceRegistration) -> Result<(), Box<dyn std::error::Error>> {
        let url = format!("{}/v1/agent/service/register", self.consul_url);
        
        self.client.put(&url).json(&registration).send().await?;
        
        tracing::info!("Service registered: {}", registration.id);
        Ok(())
    }

    pub async fn deregister_service(&self, service_id: &str) -> Result<(), Box<dyn std::error::Error>> {
        let url = format!("{}/v1/agent/service/deregister/{}", self.consul_url, service_id);
        self.client.put(&url).send().await?;
        
        tracing::info!("Service deregistered: {}", service_id);
        Ok(())
    }

    pub async fn discover_service(&self, service_name: &str) -> Result<Vec<String>, Box<dyn std::error::Error>> {
        let url = format!("{}/v1/health/service/{}?passing", self.consul_url, service_name);
        
        let response = self.client
            .get(&url)
            .send()
            .await?
            .json::<Vec<serde_json::Value>>()
            .await?;

        let addresses: Vec<String> = response
            .iter()
            .filter_map(|entry| {
                let service = &entry["Service"];
                let address = service["Address"].as_str()?;
                let port = service["Port"].as_u64()?;
                Some(format!("http://{}:{}", address, port))
            })
            .collect();

        Ok(addresses)
    }
}

