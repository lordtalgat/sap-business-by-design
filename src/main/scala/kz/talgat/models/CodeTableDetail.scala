package kz.talgat.models

case class CodeTableDetail(id: Int,
                                          codeTableId: Int,
                                          code: String,
                                          description: String,
                                          attribute1: String,
                                          attribute2: String,
                                          attribute3: String,
                                          attribute4: String,
                                          attribute5: String,
                                          isDefault: Boolean,
                                          isActive: Boolean)
