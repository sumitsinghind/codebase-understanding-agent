import os
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings

documents = []

repo_path = "repo_files"

for root, dirs, files in os.walk(repo_path):
    for file in files:
        if file.endswith((".java", ".js", ".py", ".ts")):
            path = os.path.join(root, file)
            loader = TextLoader(path, encoding="utf-8")
            documents.extend(loader.load())

print("Loaded files:", len(documents))

text_splitter = RecursiveCharacterTextSplitter(
    chunk_size=800,
    chunk_overlap=150
)

docs = text_splitter.split_documents(documents)

print("Total chunks:", len(docs))

embeddings = HuggingFaceEmbeddings()

vectorstore = FAISS.from_documents(docs, embeddings)

vectorstore.save_local("vector_db")

print("✅ Codebase indexed successfully!")