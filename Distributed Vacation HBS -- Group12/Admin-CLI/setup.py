"""
setup.py

Setup script for the Admin CLI tool of Distributed Vacation HBS (Hotel Booking System).
Defines packaging and dependencies for installation.

Contributors:
- Mohammad Shajadul Karim (Admin-CLI)
- Pavan Sai Kappiri (Team Lead)
- Shahinur Rahman (Auth Microservice)
- Mohammed Wafiul Abire Aonkon (Listings Microservice)
"""
from setuptools import setup, find_packages

setup(
    name="admin-cli",
    version="0.1.0",
    packages=find_packages(),
    include_package_data=True,
    install_requires=[
        "click==8.1.7",
        "requests==2.31.0",
        "tabulate==0.9.0",
        "rich==13.7.0",
        "pytest==7.4.3",
    ],
    entry_points={
        "console_scripts": [
            "admin-cli=admin_cli:cli",
        ],
    },
) 