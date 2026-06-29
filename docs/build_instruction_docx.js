const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType, ShadingType
} = require("docx");

const FONT = "Arial";
const border = { style: BorderStyle.SINGLE, size: 4, color: "BBBBBB" };
const borders = { top: border, bottom: border, left: border, right: border };
const cellMargins = { top: 80, bottom: 80, left: 120, right: 120 };

function h1(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(text)] });
}
function h2(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(text)] });
}
function p(text, opts = {}) {
  return new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text, ...opts })] });
}
function bullet(runs) {
  return new Paragraph({ numbering: { reference: "bullets", level: 0 }, spacing: { after: 60 },
    children: Array.isArray(runs) ? runs : [new TextRun(runs)] });
}
function num(ref, runs) {
  return new Paragraph({ numbering: { reference: ref, level: 0 }, spacing: { after: 60 },
    children: Array.isArray(runs) ? runs : [new TextRun(runs)] });
}
function cell(text, { width, bold = false, fill } = {}) {
  return new TableCell({
    borders, width: { size: width, type: WidthType.DXA }, margins: cellMargins,
    shading: fill ? { fill, type: ShadingType.CLEAR } : undefined,
    children: [new Paragraph({ children: [new TextRun({ text, bold })] })],
  });
}

// A4 content width with 1" margins: 11906 - 2880 = 9026
const W = 9026;

const doc = new Document({
  styles: {
    default: { document: { run: { font: FONT, size: 22 } } },
    paragraphStyles: [
      { id: "Title0", name: "Title0", basedOn: "Normal", next: "Normal",
        run: { size: 36, bold: true, font: FONT },
        paragraph: { spacing: { after: 120 } } },
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, font: FONT, color: "1F4E79" },
        paragraph: { spacing: { before: 260, after: 140 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: FONT },
        paragraph: { spacing: { before: 160, after: 100 }, outlineLevel: 1 } },
    ],
  },
  numbering: {
    config: [
      { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•",
        alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 280 } } } }] },
      { reference: "rules", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.",
        alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 320 } } } }] },
      { reference: "checklist", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.",
        alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 320 } } } }] },
    ],
  },
  sections: [{
    properties: { page: { size: { width: 11906, height: 16838 },
      margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
    children: [
      new Paragraph({ style: "Title0", children: [new TextRun("Изменение участников акта (консультанты и ресечеры)")] }),
      p("Инструкция для тимлидов и бухгалтерии: как изменить состав участников по акту и как это влияет на бонусы и маржинальную прибыль направления.", { italics: true, color: "555555" }),

      h1("1. Где это находится"),
      p("Вкладка «Выставленные акты по направлениям и людям»."),
      p("В таблице по каждому акту есть кнопка «Изменить». Она открывает окно «Изменить данные акта», где можно поменять, кто участвовал в акте: консультантов (responsible) и ресечеров."),
      p("Доступ к редактированию — у ролей тимлида/менеджера и бухгалтера."),

      h1("2. Что можно менять в окне"),
      new Table({
        width: { size: W, type: WidthType.DXA },
        columnWidths: [5400, 3626],
        rows: [
          new TableRow({ tableHeader: true, children: [
            cell("Поле", { width: 5400, bold: true, fill: "D5E8F0" }),
            cell("Можно менять", { width: 3626, bold: true, fill: "D5E8F0" }),
          ]}),
          new TableRow({ children: [ cell("Номер акта, Дата акта", { width: 5400 }), cell("нет (только просмотр)", { width: 3626 }) ]}),
          new TableRow({ children: [ cell("Сумма акта (без НДС)", { width: 5400 }), cell("да", { width: 3626 }) ]}),
          new TableRow({ children: [ cell("Кандидат", { width: 5400 }), cell("да", { width: 3626 }) ]}),
          new TableRow({ children: [ cell("Список консультантов", { width: 5400 }), cell("ФИО, направление, % участия (сумма считается сама)", { width: 3626 }) ]}),
          new TableRow({ children: [ cell("Список ресечеров", { width: 5400 }), cell("ФИО, направление, % участия (сумма считается сама)", { width: 3626 }) ]}),
        ],
      }),
      p(""),
      p("Кнопки: «Добавить ресечера», «Удалить» (у каждой строки), «Сохранить».", { }),

      h2("Как изменить консультанта"),
      bullet("Выберите другого человека в строке консультанта, либо нажмите «Удалить» и добавьте нужного."),
      bullet([ new TextRun("Укажите % участия. Сумма в рублях подставится автоматически: "), new TextRun({ text: "сумма = сумма акта без НДС × % / 100.", bold: true }) ]),

      h2("Как изменить / добавить ресечера"),
      bullet("Нажмите «Добавить ресечера» или поменяйте ФИО в существующей строке."),
      bullet("Выберите направление ресечера и укажите % участия — сумма посчитается автоматически."),

      h1("3. Правила, которые проверяет система при сохранении"),
      num("rules", [ new TextRun({ text: "Сумма процентов участия консультантов должна быть равна 100%. ", bold: true }), new TextRun("Если после удаления одного консультанта у оставшегося осталось, например, 30% — сохранить не получится (появится сообщение «Сумма процентов участия консультантов должна быть равна 100%»). Поставьте оставшемуся 100% или распределите между несколькими так, чтобы в сумме было 100%.") ]),
      num("rules", "У каждого консультанта должен быть указан процент (больше 0)."),
      num("rules", "Ресечеров не может быть больше, чем консультантов."),
      num("rules", "Суммы пересчитываются автоматически из процента и суммы акта — вручную их подгонять не нужно."),
      p("Пример. В акте было: Консультант А — 30%, Консультант Б — 70%. Удаляем Б, ставим А — 100%. Сумма у А автоматически станет равна полной сумме акта без НДС. Сохраняем.", { italics: true, color: "555555" }),

      h1("4. Как это учитывается в бонусах"),
      bullet("Бонус нового / изменённого участника считается по дате внесения правки — попадает в тот месяц (и квартал), когда вы сделали изменение, и рассчитывается по его текущей ставке."),
      bullet("Ставка ресечера зависит от его суммарного месячного оборота (всех его актов за месяц), а не от одного акта. Поэтому добавление акта может поднять процент и по другим его актам этого месяца — это нормально."),
      bullet("Пример: акт за март, ресечера заменили в июне → бонус нового ресечера попадёт в июнь, а не в март."),

      h1("5. Как это учитывается в маржинальной прибыли направления"),
      p("Маржа направления = доход − расход (зарплаты и заработанные бонусы). Бонусы учитываются заработанные (не дожидаясь оплаты акта клиентом)."),
      p("Когда замена происходит в уже закрытом квартале (квартал закрывается после 20-го числа месяца, следующего за кварталом: Q1 — после 20 апреля, Q2 — после 20 июля и т.д.):", { bold: true }),
      bullet("Бонус снятого участника, который уже был учтён в закрытом квартале, автоматически вычитается обратно (отменяется) в марже того квартала, когда вы внесли правку. Это нужно, чтобы один акт не «оплачивался» двумя бонусами."),
      bullet("Бонус нового участника ложится расходом в квартал правки."),
      bullet("Доход по акту остаётся за его исходным кварталом (закрытый квартал не пересчитывается)."),
      p("Пример (не прошёл испытательный срок). Акт марта, направление Industrial. В марте бонус заработала Консультант Б (учтён в I квартале). В июне кандидат не прошёл испытательный — акт переделал другой человек. При сохранении правки система: вернёт бонус Б в маржу II квартала (раз I квартал уже закрыт) и учтёт бонус нового участника в II квартале. В результате по акту в марже остаётся только бонус того, кто реально сделал работу.", { italics: true, color: "555555" }),
      p("Никаких дополнительных действий для этого делать не нужно — корректировка формируется автоматически в момент сохранения правки."),

      h1("6. Короткий чек-лист"),
      num("checklist", "Вкладка «Выставленные акты по направлениям и людям» → «Изменить»."),
      num("checklist", "Поменяйте/удалите/добавьте консультантов и ресечеров."),
      num("checklist", "Проверьте: % участия консультантов в сумме = 100%."),
      num("checklist", "Суммы пересчитаются сами — проверьте, что они верные."),
      num("checklist", "«Сохранить»."),
      num("checklist", "Бонусы и маржа пересчитаются автоматически: новый участник — по дате правки, у снятого из закрытого квартала бонус вычитается обратно в квартале правки."),
    ],
  }],
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(process.argv[2] || "instruction.docx", buffer);
  console.log("OK");
});
