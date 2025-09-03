from setuptools import setup, find_packages

setup(
    name="lazyfatpandas",
    version="0.1",
    packages=find_packages(),
    install_requires=[
        # "pandas",
    ],
    author="Bhushan, Chiranmoy, Priyesh, Utkarsh, Sudarshan",
    author_email="singhbhushan@cse.iitb.ac.in",
    description="Your custom lazy pandas wrapper",
    long_description=open("readme.md").read(),
    long_description_content_type="text/markdown",
    classifiers=[
        "Programming Language :: Python :: 3",
    ],
)
